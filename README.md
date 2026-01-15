# FoodHub - Online Food Ordering System

A modern, full-stack web application for online food ordering with separate user and admin interfaces. Built with Java Servlets, JSP, and vanilla JavaScript.

## Features

### User Frontend (Customer UI)
- **Browse Products**: View menu items with categories (Appetizers, Main Course, Desserts, Beverages)
- **Shopping Cart**: Add items to cart, adjust quantities, view cart summary
- **Place Orders**: Complete checkout with delivery information and payment method selection
- **View Orders**: Track order history and current order status
- **User Authentication**: Login/Register system with session management
- **Responsive Design**: Modern UI with smooth animations and transitions

### Admin Frontend (Admin UI)
- **Product Management**: 
  - List all products with filtering and search
  - Add new products with details and images
  - Edit existing product information
  - Delete products from catalog
- **Order Management**:
  - View all customer orders
  - Filter orders by status (Pending, Confirmed, Preparing, Ready, Delivered, Cancelled)
  - View detailed order information with items
- **User Management**:
  - View all registered users
  - Search and filter users
  - Manage user accounts
- **Dashboard**:
  - Overview of key metrics
  - Recent orders summary
  - Quick access to management functions

## Tech Stack

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Modern styling with gradients, animations, and flexbox/grid layouts
- **JavaScript (ES6+)** - Async/await, fetch API, DOM manipulation
- **Google Fonts** - Playfair Display & Outfit

### Backend
- **Java** - Core language
- **Java Servlets** - Request handling and business logic
- **JSP** - Dynamic page generation (minimal use)
- **MySQL** - Database management
- **JDBC** - Database connectivity
- **Gson** - JSON parsing and generation

### Server
- **Apache Tomcat** - Servlet container

## Project Structure

```
201Project/
├── SQL/                          # Database scripts
│   │   create_tables.sql
│   └── insert_sample_data.sql    # Database schema and initial data
├── src/
│   ├── dao/                     # Data Access Objects
│   │   ├── OrderDAO.java
│   │   ├── OrderItemDAO.java
│   │   ├── ProductDAO.java
│   │   └── UserDAO.java
│   ├── model/                   # Data models
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Product.java
│   │   └── User.java
│   ├── service/                 # Business logic layer
│   │   ├── OrderService.java
│   │   ├── UserService.java
│   │   └── ProductService.java
│   ├── servlet/                 # HTTP request handlers
│   │   ├── LoginServlet.java
│   │   ├── OrderServlet.java
│   │   └── ProductServlet.java
│   └── util/                    # Utility classes
│       └── DBConnection.java
└── web/                         # Frontend files
    ├── pages/                   # HTML pages
    │   ├── index.html          # Homepage
    │   ├── product.html        # Product catalog
    │   ├── orders.html         # User order history
    │   ├── about.html          # checkout page
    │   ├── checkout.html       # About page
    │   ├── contact.html        # Contact page
    │   ├── login.html          # Login page
    │   ├── register.html       # Registration page
    │   └── admin.html          # Admin dashboard
    ├── css/                     # Stylesheets
    │   ├── index.css
    │   ├── product.css
    │   ├── orders.css
    │   ├── about.css
    │   ├── contact.css
    │   ├── checkout.css
    │   ├── login.css
    │   └── admin.css
    └── js/                      # JavaScript files
        ├── index.js
        ├── product.js
        ├── orders.js
        ├── about.js
        ├── contact.js
        ├── login.js
        ├── checkout.js
        └── admin.js
```

## Database Schema

### Tables
- **USERS** - User accounts (customers and admins)
- **PRODUCTS** - Food items catalog
- **ORDERS** - Customer orders
- **ORDER_ITEMS** - Items in each order

### Key Relationships
- Orders → Users (many-to-one)
- Order Items → Orders (many-to-one)
- Order Items → Products (many-to-one)

## Setup Instructions

### Prerequisites
- Java JDK 25
- Apache Tomcat 9.x 
- Oracle Database
- IDE (IntelliJ IDEA)

### Application Setup
1. Clone the repository
2. Import project into your IDE as a Java Web Application
3. Configure database connection in `DBConnection.java`:
   - Update database URL, username, and password
4. Add Oracle Connector and Gson library to project dependencies
5. Deploy to Tomcat server
6. Access application at `http://localhost:8080/201Project/`

### Default Admin Account
- Username: `admin`
- Password: `123456`
- Role: `admin`

### Test User Account
- Username: `Cookie`
- Email: `fangsiying@student.usm.my`
- Password: 123456

## API Endpoints

### User Management
- `POST /LoginServlet?action=login` - User login
- `POST /RegisterServlet` - User registration
- `GET /LoginServlet?action=checkSession` - Check login status
- `GET /LoginServlet?action=logout` - User logout

### Product Management
- `GET /ProductServlet?action=list` - List all products
- `GET /ProductServlet?action=get&id={id}` - Get product details
- `POST /ProductServlet?action=add` - Add new product (admin)
- `POST /ProductServlet?action=update` - Update product (admin)
- `POST /ProductServlet?action=delete` - Delete product (admin)

### Order Management
- `POST /OrderServlet?action=create` - Create new order
- `GET /OrderServlet?action=list` - Get user's orders
- `GET /OrderServlet?action=listAll` - Get all orders (admin)
- `GET /OrderServlet?action=get&id={id}` - Get order details
- `POST /OrderServlet?action=updateStatus` - Update order status (admin)

## Key Features Implementation

### Session Management
- Server-side session tracking using HttpSession
- Automatic session validation on protected routes
- User role-based access control (customer vs admin)

### Shopping Cart
- Client-side cart storage using localStorage
- Cart persistence across sessions
- Real-time cart updates and total calculation

### Order Processing
- Multi-step checkout flow
- Order validation and stock checking
- Automatic order status tracking
- Email-style order confirmation

### Security
- Password hashing (implementation ready)
- SQL injection prevention using PreparedStatements
- XSS protection through input validation
- CSRF token support (can be added)

## Future Enhancements
- Payment gateway integration
- Real-time order tracking with WebSocket
- Product image upload functionality
- Email notifications for order updates
- Reviews and ratings system
- Advanced search and filtering
- Multi-language support
- Mobile app version

## Development Notes
- Follow MVC architecture pattern
- Use DAO pattern for database operations
- Implement proper error handling and logging
- Maintain separation of concerns
- Write clean, documented code

## Contributors
- Xiao Kang - Java Backend Logic
- Jiang Zhiqian - Frontend Developer (Admin Module)
- Fang Siying - Database, DAO & Project Manager
- Lu Yajin - Frontend Developer (User Module)


## License
Created for CAT201 Project - 2025

## Contact
For questions or support, contact: fangsiying@student.usm.my

---

**Note**: This is an academic project developed for learning purposes. Not intended for production use without proper security auditing and enhancements.
