#!/bin/bash

# Database configuration
export DB_HOST="url-shortener-db.co34aokqgeb7.us-east-1.rds.amazonaws.com"
export DB_PORT="5432"
export DB_NAME="url_shortener"
export DB_USERNAME="postgres"  # Replace with your actual RDS username
export DB_PASSWORD="your_password_here"  # Replace with your actual RDS password

# Application configuration
export APP_SERVER_URL="http://localhost:8080" 