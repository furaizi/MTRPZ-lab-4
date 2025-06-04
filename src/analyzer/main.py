"""
URL Shortener Analytics Dashboard
Provides real-time analytics and monitoring for the URL shortener service
"""

from fastapi import FastAPI, Request, HTTPException, Query, Depends
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
import asyncpg
import asyncio
from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional
import os
from urllib.parse import urlparse
import logging
from contextlib import asynccontextmanager

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Database configuration
DATABASE_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", "5432")),
    "user": os.getenv("DB_USER", "user"),
    "password": os.getenv("DB_PASSWORD", "password"),
    "database": os.getenv("DB_NAME", "urlshortener")
}

class DatabaseManager:
    def __init__(self):
        self.pool = None
        self._lock = asyncio.Lock()
    
    async def init_pool(self):
        """Initialize connection pool with retry logic"""
        max_retries = 5
        retry_delay = 5  # seconds
        
        for attempt in range(max_retries):
            try:
                async with self._lock:
                    if self.pool is None:
                        self.pool = await asyncpg.create_pool(**DATABASE_CONFIG)
                        logger.info("Database connection pool initialized")
                        return
            except Exception as e:
                if attempt == max_retries - 1:
                    logger.error(f"Failed to initialize database pool after {max_retries} attempts: {e}")
                    raise
                logger.warning(f"Failed to initialize database pool (attempt {attempt + 1}/{max_retries}): {e}")
                await asyncio.sleep(retry_delay)
    
    async def close_pool(self):
        """Close connection pool"""
        if self.pool:
            await self.pool.close()
            logger.info("Database connection pool closed")
    
    @asynccontextmanager
    async def get_connection(self):
        """Get database connection from pool with automatic retry"""
        if not self.pool:
            await self.init_pool()
        
        try:
            async with self.pool.acquire() as connection:
                yield connection
        except asyncpg.exceptions.ConnectionDoesNotExistError:
            logger.warning("Connection lost, reinitializing pool")
            await self.init_pool()
            async with self.pool.acquire() as connection:
                yield connection

db_manager = DatabaseManager()

# Initialize FastAPI app
app = FastAPI(
    title="URL Shortener Analytics",
    description="Analytics dashboard for URL shortener service",
    version="1.0.0",
    docs_url="/api/docs",
    redoc_url="/api/redoc"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Static files and templates
app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")

class AnalyticsService:
    """Service for fetching and analyzing URL shortener data"""
    
    @staticmethod
    async def get_links_created_by_days(days: int = 30) -> List[Dict[str, Any]]:
        """Get number of links created per day for the last N days"""
        async with db_manager.get_connection() as conn:
            query = """
            WITH RECURSIVE dates AS (
                SELECT CURRENT_DATE - (interval '1 day' * $1) AS date
                UNION ALL
                SELECT date + INTERVAL '1 day'
                FROM dates
                WHERE date < CURRENT_DATE
            ),
            daily_counts AS (
                SELECT 
                    DATE(created_at) as date,
                    COUNT(*) as count
                FROM link 
                WHERE created_at >= CURRENT_DATE - (interval '1 day' * $2)
                GROUP BY DATE(created_at)
            )
            SELECT 
                dates.date::date as date,
                COALESCE(daily_counts.count, 0) as count
            FROM dates
            LEFT JOIN daily_counts ON dates.date = daily_counts.date
            ORDER BY dates.date
            """
            
            rows = await conn.fetch(query, days, days)
            return [{"date": row["date"].isoformat(), "count": row["count"]} for row in rows]
    
    @staticmethod
    async def get_top_domains(limit: int = 5) -> List[Dict[str, Any]]:
        """Get top N domains by number of shortened URLs"""
        async with db_manager.get_connection() as conn:
            query = """
            WITH domain_stats AS (
                SELECT 
                    CASE 
                        WHEN original_url ~ '^https?://' THEN 
                            regexp_replace(
                                regexp_replace(original_url, '^https?://', ''),
                                '/.*$', ''
                            )
                        ELSE 
                            regexp_replace(original_url, '/.*$', '')
                    END as domain,
                    COUNT(*) as url_count,
                    SUM(clicks) as total_clicks,
                    SUM(unique_visitors) as total_visitors,
                    AVG(clicks)::float as avg_clicks
                FROM link 
                WHERE is_active = true
                GROUP BY domain
            )
            SELECT 
                domain,
                url_count,
                total_clicks,
                total_visitors,
                avg_clicks
            FROM domain_stats
            ORDER BY url_count DESC, total_clicks DESC
            LIMIT $1
            """
            
            rows = await conn.fetch(query, limit)
            return [
                {
                    "domain": row["domain"],
                    "url_count": row["url_count"],
                    "total_clicks": row["total_clicks"],
                    "total_visitors": row["total_visitors"],
                    "avg_clicks": float(row["avg_clicks"])
                } 
                for row in rows
            ]
    
    @staticmethod
    async def get_recent_links(
        limit: int = 20,
        domain_filter: Optional[str] = None,
        sort_by: str = "created_at",
        sort_order: str = "desc"
    ) -> List[Dict[str, Any]]:
        """Get recent links with filtering and sorting"""
        valid_sort_fields = {
            "created_at": "created_at",
            "clicks": "clicks",
            "unique_visitors": "unique_visitors",
            "last_accessed": "last_accessed_at"
        }
        
        if sort_by not in valid_sort_fields:
            sort_by = "created_at"
        
        sort_order = "DESC" if sort_order.lower() == "desc" else "ASC"
        
        async with db_manager.get_connection() as conn:
            base_query = """
            SELECT 
                short_code,
                original_url,
                clicks,
                unique_visitors,
                created_at,
                last_accessed_at,
                is_active
            FROM link 
            WHERE 1=1
            """
            
            params = []
            if domain_filter:
                base_query += " AND original_url ILIKE $1"
                params.append(f"%{domain_filter}%")
            
            base_query += f" ORDER BY {valid_sort_fields[sort_by]} {sort_order}"
            base_query += f" LIMIT ${len(params) + 1}"
            params.append(limit)
            
            rows = await conn.fetch(base_query, *params)
            return [
                {
                    "short_code": row["short_code"],
                    "original_url": row["original_url"],
                    "clicks": row["clicks"],
                    "unique_visitors": row["unique_visitors"],
                    "created_at": row["created_at"].isoformat(),
                    "last_accessed_at": row["last_accessed_at"].isoformat() if row["last_accessed_at"] else None,
                    "is_active": row["is_active"]
                }
                for row in rows
            ]
    
    @staticmethod
    async def get_summary_stats() -> Dict[str, Any]:
        """Get comprehensive summary statistics"""
        async with db_manager.get_connection() as conn:
            query = """
            SELECT 
                COUNT(*) as total_links,
                COUNT(*) FILTER (WHERE is_active = true) as active_links,
                SUM(clicks) as total_clicks,
                SUM(unique_visitors) as total_unique_visitors,
                AVG(clicks)::float as avg_clicks_per_link,
                MAX(clicks) as max_clicks,
                COUNT(*) FILTER (WHERE clicks = 0) as unused_links,
                COUNT(DISTINCT CASE 
                    WHEN original_url ~ '^https?://' THEN 
                        regexp_replace(
                            regexp_replace(original_url, '^https?://', ''),
                            '/.*$', ''
                        )
                    ELSE 
                        regexp_replace(original_url, '/.*$', '')
                END) as unique_domains
            FROM link
            """
            
            row = await conn.fetchrow(query)
            return {
                "total_links": row["total_links"],
                "active_links": row["active_links"],
                "total_clicks": row["total_clicks"] or 0,
                "total_unique_visitors": row["total_unique_visitors"] or 0,
                "avg_clicks_per_link": float(row["avg_clicks_per_link"] or 0),
                "max_clicks": row["max_clicks"] or 0,
                "unused_links": row["unused_links"],
                "unique_domains": row["unique_domains"]
            }

@app.on_event("startup")
async def startup_event():
    """Initialize database connection on startup"""
    await db_manager.init_pool()

@app.on_event("shutdown")
async def shutdown_event():
    """Close database connection on shutdown"""
    await db_manager.close_pool()

@app.get("/", response_class=HTMLResponse)
async def dashboard(request: Request):
    """Main dashboard page"""
    try:
        # Get all analytics data concurrently
        summary_stats, links_by_days, top_domains, recent_links = await asyncio.gather(
            AnalyticsService.get_summary_stats(),
            AnalyticsService.get_links_created_by_days(30),
            AnalyticsService.get_top_domains(5),
            AnalyticsService.get_recent_links(20)
        )
        
        return templates.TemplateResponse(
            "dashboard.html",
            {
                "request": request,
                "summary_stats": summary_stats,
                "links_by_days": links_by_days,
                "top_domains": top_domains,
                "recent_links": recent_links,
                "datetime": datetime
            }
        )
    except Exception as e:
        logger.error(f"Error loading dashboard: {e}")
        raise HTTPException(
            status_code=500,
            detail="Failed to load dashboard. Please try again later."
        )

@app.get("/api/analytics/links-by-days")
async def api_links_by_days(days: int = Query(30, ge=1, le=365)):
    """API endpoint for links created by days"""
    try:
        data = await AnalyticsService.get_links_created_by_days(days)
        return {"data": data}
    except Exception as e:
        logger.error(f"Error fetching links by days: {e}")
        raise HTTPException(
            status_code=500,
            detail="Failed to fetch links data"
        )

@app.get("/api/analytics/top-domains")
async def api_top_domains(limit: int = Query(5, ge=1, le=20)):
    """API endpoint for top domains"""
    try:
        data = await AnalyticsService.get_top_domains(limit)
        return {"data": data}
    except Exception as e:
        logger.error(f"Error fetching top domains: {e}")
        raise HTTPException(
            status_code=500,
            detail="Failed to fetch domain statistics"
        )

@app.get("/api/analytics/recent-links")
async def api_recent_links(
    limit: int = Query(20, ge=1, le=100),
    domain: Optional[str] = Query(None),
    sort_by: str = Query("created_at"),
    sort_order: str = Query("desc")
):
    """API endpoint for recent links with sorting and filtering"""
    try:
        data = await AnalyticsService.get_recent_links(
            limit=limit,
            domain_filter=domain,
            sort_by=sort_by,
            sort_order=sort_order
        )
        return {"data": data}
    except Exception as e:
        logger.error(f"Error fetching recent links: {e}")
        raise HTTPException(
            status_code=500,
            detail="Failed to fetch recent links"
        )

@app.get("/api/analytics/summary")
async def api_summary():
    """API endpoint for summary statistics"""
    try:
        data = await AnalyticsService.get_summary_stats()
        return {"data": data}
    except Exception as e:
        logger.error(f"Error fetching summary stats: {e}")
        raise HTTPException(
            status_code=500,
            detail="Failed to fetch summary statistics"
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)