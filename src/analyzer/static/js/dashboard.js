// Chart configurations and data fetching
let linksChart = null;
let domainsChart = null;

// Initialize charts
function initCharts() {
    // Links created chart
    const linksCtx = document.getElementById('linksChart').getContext('2d');
    linksChart = new Chart(linksCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Links Created',
                data: [],
                borderColor: '#667eea',
                backgroundColor: 'rgba(102, 126, 234, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });

    // Top domains chart
    const domainsCtx = document.getElementById('domainsChart').getContext('2d');
    domainsChart = new Chart(domainsCtx, {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: [
                    '#667eea',
                    '#764ba2',
                    '#63b3ed',
                    '#4fd1c5',
                    '#68d391'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// Fetch and update data
async function fetchData() {
    try {
        // Update links by days chart
        const linksResponse = await fetch('/api/analytics/links-by-days');
        const linksData = await linksResponse.json();
        updateLinksChart(linksData.data);

        // Update top domains chart
        const domainsResponse = await fetch('/api/analytics/top-domains');
        const domainsData = await domainsResponse.json();
        updateDomainsChart(domainsData.data);

        // Update recent links table
        await updateRecentLinks();

        // Update last updated time
        document.getElementById('lastUpdated').textContent = 
            `Last updated: ${new Date().toLocaleString()}`;
    } catch (error) {
        console.error('Error fetching data:', error);
        showError('Failed to fetch analytics data');
    }
}

// Update charts with new data
function updateLinksChart(data) {
    const labels = data.map(item => new Date(item.date).toLocaleDateString());
    const values = data.map(item => item.count);

    linksChart.data.labels = labels;
    linksChart.data.datasets[0].data = values;
    linksChart.update();
}

function updateDomainsChart(data) {
    const labels = data.map(item => item.domain);
    const values = data.map(item => item.url_count);

    domainsChart.data.labels = labels;
    domainsChart.data.datasets[0].data = values;
    domainsChart.update();
}

// Update recent links table
async function updateRecentLinks() {
    const domainFilter = document.getElementById('domainFilter').value;
    const limit = document.getElementById('limitFilter').value;

    try {
        const response = await fetch(
            `/api/analytics/recent-links?limit=${limit}${domainFilter ? `&domain=${domainFilter}` : ''}`
        );
        const data = await response.json();
        
        const tbody = document.querySelector('#recentLinksTable tbody');
        tbody.innerHTML = data.data.map(link => `
            <tr>
                <td class="url-cell">
                    <a href="${link.original_url}" target="_blank" rel="noopener noreferrer">
                        ${link.original_url}
                    </a>
                </td>
                <td>${link.short_code}</td>
                <td>${link.clicks}</td>
                <td>${link.unique_visitors}</td>
                <td>${new Date(link.created_at).toLocaleString()}</td>
                <td>
                    <span class="status-badge ${link.is_active ? 'status-active' : 'status-inactive'}">
                        ${link.is_active ? 'Active' : 'Inactive'}
                    </span>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error updating recent links:', error);
        showError('Failed to update recent links');
    }
}

// Error handling
function showError(message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.querySelector('.container').prepend(alertDiv);
}

// Filter handling
function applyFilters() {
    updateRecentLinks();
}

// Auto-refresh
let refreshInterval;
function toggleAutoRefresh() {
    const button = document.getElementById('autoRefreshButton');
    if (refreshInterval) {
        clearInterval(refreshInterval);
        refreshInterval = null;
        button.innerHTML = '<i class="fas fa-sync-alt me-2"></i>Enable Auto-Refresh';
        button.classList.remove('btn-danger');
        button.classList.add('btn-success');
    } else {
        refreshInterval = setInterval(fetchData, 30000); // Refresh every 30 seconds
        button.innerHTML = '<i class="fas fa-sync-alt me-2"></i>Disable Auto-Refresh';
        button.classList.remove('btn-success');
        button.classList.add('btn-danger');
    }
}

// Initialize dashboard
document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    fetchData();
    document.getElementById('autoRefreshButton').addEventListener('click', toggleAutoRefresh);
}); 