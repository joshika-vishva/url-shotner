# ⚡ SnipLink - AI-Powered URL Shortener

A production-ready, enterprise-grade URL shortening system with AI safety checks, advanced analytics, and modern dark-themed UI.

## 🎯 Features

### Core Features
- **URL Shortening**: Generate unique 6-8 character short codes
- **Custom Slugs**: Create personalized short URLs
- **Expiry Dates**: Set automatic expiration for links
- **Password Protection**: Secure your links with passwords
- **Tags Support**: Organize URLs with custom tags
- **Enable/Disable Links**: Toggle link activation status

### Advanced Features
- **AI Safety Check**: Automatic malicious URL detection
- **Rich Analytics**: Track clicks, devices, browsers, IP addresses, and locations
- **Dashboard**: Real-time statistics with interactive Chart.js visualizations
- **Bulk Import**: Upload CSV files for batch URL creation
- **User Management**: JWT-based authentication with role-based access control

### Security
- JWT Authentication
- BCrypt Password Hashing
- CSRF Protection
- Rate Limiting
- Input Validation
- Exception Handling

## 🛠️ Tech Stack

### Backend
- Java 17+
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT (JSON Web Tokens)
- Maven

### Frontend
- HTML5
- CSS3 (Glassmorphism Design)
- JavaScript (ES6+)
- Bootstrap 5
- Chart.js 4.4.0
- Responsive Dark Theme

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/sniplink.git
cd sniplink
```

### 2. Database Setup
```sql
-- Create PostgreSQL database
CREATE DATABASE sniplink_db;

-- Update credentials in application.properties if needed
```

### 3. Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sniplink_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Generate your own JWT secret (base64 encoded)
jwt.secret=your_secure_secret_key
jwt.expiration=86400000
```

### 4. Build & Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

### URL Endpoints

#### Shorten URL
```http
POST /api/shorten
Authorization: Bearer <token>
Content-Type: application/json

{
  "url": "https://example.com/very/long/url",
  "customSlug": "my-link",
  "expiresAt": "2024-12-31T23:59:59",
  "password": "secret",
  "tags": "marketing, social"
}
```

#### Get User URLs
```http
GET /api/user/urls
Authorization: Bearer <token>
```

#### Update URL
```http
PUT /api/url/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "expiresAt": "2025-01-01T00:00:00",
  "tags": "updated, tags"
}
```

#### Delete URL
```http
DELETE /api/url/{id}
Authorization: Bearer <token>
```

#### Redirect (Access Short URL)
```http
GET /{shortCode}
```

### Analytics Endpoints

#### Get Dashboard Stats
```http
GET /api/analytics/dashboard
Authorization: Bearer <token>
```

#### Get URL Click Logs
```http
GET /api/analytics/url/{id}/clicks
Authorization: Bearer <token>
```

### Bulk Import
```http
POST /api/bulk/import
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: urls.csv
```

## 📁 Project Structure

```
sniplink/
├── src/
│   ├── main/
│   │   ├── java/com/sniplink/
│   │   │   ├── config/          # Security & CORS configuration
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── exception/       # Custom exceptions & handlers
│   │   │   ├── repository/      # Database repositories
│   │   │   ├── security/        # JWT & Security components
│   │   │   ├── service/         # Business logic layer
│   │   │   ├── util/            # Utility classes
│   │   │   └── SnipLinkApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/         # Stylesheets
│   │       │   ├── js/          # JavaScript files
│   │       │   ├── index.html
│   │       │   ├── dashboard.html
│   │       │   ├── login.html
│   │       │   └── register.html
│   │       └── application.properties
│   └── test/                     # Unit & Integration tests
├── pom.xml
└── README.md
```

## 🎨 UI Features

### Dark Theme
- Modern glassmorphism design
- Gradient backgrounds
- Smooth animations
- Responsive layout

### Dashboard
- Real-time statistics cards
- Interactive Chart.js line chart
- URL management table
- Click analytics

## 🔒 Security Features

1. **JWT Authentication**: Secure token-based auth
2. **Password Encryption**: BCrypt hashing
3. **CSRF Protection**: Enabled by default
4. **Rate Limiting**: Prevent abuse
5. **Input Validation**: Server-side validation
6. **AI Safety Check**: Malicious URL detection

## 📊 Database Schema

See [DATABASE.md](DATABASE.md) for detailed ER diagram and schema documentation.

## 🔄 System Architecture

See [FLOWCHART.md](FLOWCHART.md) for system architecture and flow diagrams.

## 🚢 Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for production deployment guide.

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=URLServiceTest

# Generate coverage report
mvn jacoco:report
```

## 📝 Usage Examples

### CSV Bulk Import Format
```csv
url,custom_slug
https://example.com/page1,example1
https://example.com/page2,example2
https://example.com/page3,
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- Your Name - Final Year Project

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Chart.js for visualization library
- Bootstrap for UI components
- PostgreSQL community

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

**Made with ❤️ for final year project**
