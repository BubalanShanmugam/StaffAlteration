# Deployment Guide

## Staff Alteration System - Production Deployment

### Prerequisites
- Java 17+ installed
- Node.js 16+ and npm installed
- PostgreSQL database (local or cloud)
- Git
- Docker (optional)

---

## Backend Deployment

### 1. Build the Backend

```bash
cd backend
gradle clean build -x test
```

Output: `build/libs/staff-alteration-system-1.0.0.jar`

### 2. Configure Environment Variables

Create a `.env` file or configure system environment variables:

```bash
# Database Configuration
DB_URL=jdbc:postgresql://your-db-host:5432/your-database
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your-very-long-secret-key-min-32-characters
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# CORS Configuration
CORS_ORIGINS=http://localhost:3000,http://your-domain.com

# Server Configuration
SERVER_PORT=8080
```

### 3. Running the Backend

#### Local Development
```bash
java -jar build/libs/staff-alteration-system-1.0.0.jar
```

#### With Environment Variables
```bash
java -jar build/libs/staff-alteration-system-1.0.0.jar \
  --spring.datasource.url=${DB_URL} \
  --spring.datasource.username=${DB_USERNAME} \
  --spring.datasource.password=${DB_PASSWORD} \
  --jwt.secret=${JWT_SECRET}
```

#### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/staff-alteration-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t staff-alteration-backend .
docker run -p 8080:8080 --env-file .env staff-alteration-backend
```

### 4. Database Migrations

Flyway automatically runs migrations on startup. Ensure the database exists:

```sql
CREATE DATABASE staffalteration;
```

Migrations will be applied automatically when the application starts.

### 5. Health Check

Test the backend:
```bash
curl http://localhost:8080/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"Staff1","password":"password123"}'
```

---

## Frontend Deployment

### 1. Build the Frontend

```bash
cd frontend
npm install
npm run build
```

Output: `dist/` folder with optimized production files

### 2. Static File Server

Serve the built files using any static server:

#### Using Node.js (http-server)
```bash
npm install -g http-server
http-server dist -p 3000 -c-1
```

#### Using Nginx
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    root /var/www/staff-alteration/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### Using Apache
```apache
<VirtualHost *:80>
    ServerName your-domain.com
    DocumentRoot /var/www/staff-alteration/dist
    
    <Directory /var/www/staff-alteration/dist>
        Options -MultiViews
        RewriteEngine On
        RewriteCond %{REQUEST_FILENAME} !-f
        RewriteRule ^ index.html [QSA,L]
    </Directory>
    
    ProxyPassMatch ^/api http://localhost:8080/api
    ProxyPassReverse ^/api http://localhost:8080/api
</VirtualHost>
```

#### Docker Deployment
```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY frontend .
RUN npm install
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3. Environment Configuration

Update `vite.config.ts` for production:
```typescript
const apiBaseURL = process.env.REACT_APP_API_URL || 'http://your-domain.com/api'
```

### 4. Testing

Test the frontend:
```bash
npm run preview
```

Open http://localhost:4173 in your browser

---

## Full Stack Deployment (Docker Compose)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: staffalteration
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: your_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/staffalteration
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: your_password
      JWT_SECRET: your-very-long-secret-key
    depends_on:
      - database
    restart: unless-stopped

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  postgres_data:
```

Deploy:
```bash
docker-compose up -d
```

---

## SSL/HTTPS Configuration

### Using Let's Encrypt with Nginx

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot certonly --nginx -d your-domain.com
```

Update Nginx config:
```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # ... rest of configuration
}

server {
    listen 80;
    return 301 https://$server_name$request_uri;
}
```

---

## Performance Optimization

### Backend
- Enable SQL query caching
- Configure connection pooling
- Use CDN for static assets
- Enable GZIP compression

### Frontend
- Enable code splitting
- Minify CSS/JS
- Use production build
- Enable browser caching
- Lazy load images

---

## Monitoring & Maintenance

### Logs
```bash
# Backend logs
tail -f application.log

# Check backend health
curl http://localhost:8080/api/actuator/health
```

### Database Backups
```bash
# PostgreSQL backup
pg_dump -U postgres staffalteration > backup.sql

# Restore
psql -U postgres staffalteration < backup.sql
```

### Security Updates
```bash
# Update dependencies
cd backend && gradle dependencies --update
cd frontend && npm outdated
npm update
```

---

## Troubleshooting

### Backend won't start
- Check database connection
- Verify environment variables
- Check Java version (17+)
- Review application logs

### Frontend shows blank page
- Check browser console for errors
- Verify API endpoint configuration
- Clear browser cache
- Check network requests

### Database issues
- Verify Flyway migrations
- Check PostgreSQL is running
- Verify credentials
- Check disk space

---

## Production Checklist

- [ ] Database backed up
- [ ] SSL certificates installed
- [ ] Environment variables set securely
- [ ] JWT secret changed (min 32 characters)
- [ ] CORS origins configured correctly
- [ ] Database credentials changed from defaults
- [ ] Firewall rules configured
- [ ] Health checks configured
- [ ] Log rotation configured
- [ ] Monitoring/alerting setup
- [ ] API rate limiting enabled
- [ ] Database connection pooling optimized

---

## Support & Rollback

### Rolling Back
```bash
# Get previous version
git checkout previous-tag
gradle clean build

# Deploy previous JAR
java -jar previous-version.jar
```

### Emergency Stop
```bash
# Kill backend process
ps aux | grep java
kill -9 <PID>

# Or use Docker
docker-compose down
```

---

**Last Updated**: December 28, 2025
**Version**: 1.0.0
