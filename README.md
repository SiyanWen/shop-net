# RUN
## 1. Start database and kafka container
in root folder:
```
docker compose -f docker-compose.yml up -d
```
## 2. Start backend servers
```
mvn clean install -f        common-security/pom.xml -q && mvn clean compile -f        ItemService/pom.xml -q && mvn clean compile -f CartService/pom.xml -q && mvn clean compile -f OrderService/pom.xml -q && mvn clean compile -f PaymentService/pom.xml -q && mvn clean compile -f AccountService/pom.xml
```
in each service's root folder:
```
./mvnw spring-boot:run
```
## 3. Start frontend
in shop-net folder under root folder:
```
npm start
```
The application is available on http://localhost:3000/


# Functional Requirements
- User signin/signup (Account Service)
- Add items to cart (Item Service, Cart Service:)
- Clear cart (Cart Service)
- Inventory update (Item Service)
- Order CRUD (Order Service)
- Payment CRUD (Payment Service)

**Architecture diagram**
![architecture](./img/architecture.jpg)

**User Flow**

![userflow](./img/UserFlow.jpg)

**Data Flow**

![dataflow](./img/DataFlow.jpg)

**Data Models**

![datamodels](./img/DataModels.jpg)

**Status code:**

![dataflow](./img/statusCode.jpg)

# API design
## Account Service

### 1. Signup
- this will save a user to users and user_roles table.
#### POST http://localhost:8090/api/v1/auth/jwt/signup
Request body:
```
{
  "username": "steve",
  "email": "steve@gmail.com",
  "password": "123",
  "shippingAddress": "123 Steve Rd.",
  "role": [
    "ROLE_USER"
  ]
}
```
Response : 201
```
User registered successfully
```

### 2. Signin
- This will return a JWT token.
#### POST http://localhost:8090/api/v1/auth/jwt/signin
Request body:
```
{
  "accountOrEmail": "steve",
  "password": "123"
}
```
Response: 200
```
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdGV2ZUBnbWFpbC5jb20iLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzcxODE5NjQzLCJleHAiOjE3NzI0MjQ0NDN9.gDHid-moohqApfB7oorkHkAmvxbekc5tcXvIAueyAr4",
  "tokenType": "JWT"
}
```
### 3. getUserByUsername
- this is for other services to get user info by username
#### GET  http://localhost:9090/api/v1/auth/jwt/user?username={username}
Response: 200
```
{
  "email": "steve@gmail.com",
  "username": "steve",
  "roles": [
    "ROLE_USER"
  ],
  "shippingAddress": "123 Steve Rd.",
  "id": "161b3fce-25c0-4dae-beb3-9f89f00689cb"
}
```

## Item Service

### 1. getAllItems
#### GET  http://localhost:8092/api/items
Response: 200
```
[
  {
    "id": "69975e9906a586cc6989b03d",
    "itemId": "ITEM-001",
    "name": "Chicken Fries - 9 Pc",
    "unitPrice": 4.89,
    "pictureUrls": [
      "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f439436f-c5ab-47af-bac4-7b73ab60a24b-retina-large.jpg"
    ],
    "upc": "012345678901",
    "stockQty": 48,
    "createdAt": "2026-02-19T19:03:53.685Z",
    "updatedAt": "2026-02-19T19:06:51.374Z"
  },
  {
    "id": "69975e9906a586cc6989b03e",
    "itemId": "ITEM-002",
    "name": "Whopper Meal",
    "unitPrice": 10.59,
    "pictureUrls": [
      "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f878a689-618b-4c70-a00f-e7b1f320adc9-retina-large.jpg"
    ],
    "upc": "012345678902",
    "stockQty": 119,
    "createdAt": "2026-02-19T19:03:53.685Z",
    "updatedAt": "2026-02-19T19:06:14.563Z"
  },
  ...
    {
    "id": "69975e9906a586cc6989b05a",
    "itemId": "ITEM-030",
    "name": "Stir Fried A-Choy with Minced Garlic",
    "unitPrice": 10.99,
    "pictureUrls": [
      "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/a307e73d-dd12-4841-be14-6f5825a64c59-retina-large.jpg"
    ],
    "upc": "012345678930",
    "stockQty": 200,
    "createdAt": "2026-02-19T19:03:53.685Z",
    "updatedAt": "2026-02-19T19:03:53.685Z"
  }
]
```
### 2. get all items (paginated)

#### GET  http://localhost:8092/api/items/paginated?page=0&size=8
Response: 200
```
{
  "content": [
    {
      "id": "69975e9906a586cc6989b03d",
      "itemId": "ITEM-001",
      "name": "Chicken Fries - 9 Pc",
      "unitPrice": 4.89,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f439436f-c5ab-47af-bac4-7b73ab60a24b-retina-large.jpg"
      ],
      "upc": "012345678901",
      "stockQty": 48,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:06:51.374Z"
    },
    {
      "id": "69975e9906a586cc6989b03e",
      "itemId": "ITEM-002",
      "name": "Whopper Meal",
      "unitPrice": 10.59,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f878a689-618b-4c70-a00f-e7b1f320adc9-retina-large.jpg"
      ],
      "upc": "012345678902",
      "stockQty": 119,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:06:14.563Z"
    },
    {
      "id": "69975e9906a586cc6989b03f",
      "itemId": "ITEM-003",
      "name": "Impossible™ Whopper",
      "unitPrice": 7.99,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/5c306a5f-fdd2-41d2-a660-9762aaa8eee8-retina-large.jpg"
      ],
      "upc": "012345678903",
      "stockQty": 200,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:03:53.685Z"
    },
    {
      "id": "69975e9906a586cc6989b040",
      "itemId": "ITEM-004",
      "name": "HERSHEYS® Sundae Pie",
      "unitPrice": 3.09,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/80b1670d-e9c0-4886-a5b7-1ad48edd24ca-retina-large.jpg"
      ],
      "upc": "012345678904",
      "stockQty": 50,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:03:53.685Z"
    },
    {
      "id": "69975e9906a586cc6989b041",
      "itemId": "ITEM-005",
      "name": "Whopper",
      "unitPrice": 6.39,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/9b3d7985-e457-43b3-938d-5184f48c2687-retina-large-jpeg"
      ],
      "upc": "012345678905",
      "stockQty": 120,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:03:53.685Z"
    },
    {
      "id": "69975e9906a586cc6989b042",
      "itemId": "ITEM-006",
      "name": "Double Whopper Meal",
      "unitPrice": 11.69,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/45addf4a-e8a8-47cb-a705-cce1d10ce86d-retina-large.jpg"
      ],
      "upc": "012345678906",
      "stockQty": 200,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:03:53.685Z"
    },
    {
      "id": "69975e9906a586cc6989b043",
      "itemId": "ITEM-007",
      "name": "Spicy Crispy Chicken Sandwich",
      "unitPrice": 3.09,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/31dd68c2-06ec-42ad-bcd4-da7bd3425437-retina-large-jpeg"
      ],
      "upc": "012345678907",
      "stockQty": 49,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:06:51.454Z"
    },
    {
      "id": "69975e9906a586cc6989b044",
      "itemId": "ITEM-008",
      "name": "Whopper",
      "unitPrice": 6.09,
      "pictureUrls": [
        "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/3e437f54-fa4e-4e9d-bf80-8a1e5b120f32-retina-large-jpeg"
      ],
      "upc": "012345678908",
      "stockQty": 120,
      "createdAt": "2026-02-19T19:03:53.685Z",
      "updatedAt": "2026-02-19T19:03:53.685Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 8,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "last": false,
  "totalElements": 30,
  "totalPages": 4,
  "size": 8,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "first": true,
  "numberOfElements": 8,
  "empty": false
}
```
### 3. get item by itemId

#### GET  http://localhost:8092/api/items/{itemId}

example: http://localhost:8092/api/items/ITEM-001
Respond: 200
```
{
  "id": "69975e9906a586cc6989b03d",
  "itemId": "ITEM-001",
  "name": "Chicken Fries - 9 Pc",
  "unitPrice": 4.89,
  "pictureUrls": [
    "https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f439436f-c5ab-47af-bac4-7b73ab60a24b-retina-large.jpg"
  ],
  "upc": "012345678901",
  "stockQty": 48,
  "createdAt": "2026-02-19T19:03:53.685Z",
  "updatedAt": "2026-02-19T19:06:51.374Z"
}
```

### 4. update inventory when checking out 
Authorization required, only admin and service

#### PATCH http://localhost:8092/api/items/{itemId}/inventory
Example: http://localhost:8092/api/items/ITEM-001/inventory
Request body:

```
{
    "quantity":4
}
```
## Cart Service (Authorization required)
### 1. getCart
#### http://localhost:8094/api/cart?username=...
Response: 200
```
{
  "cartId": 2,
  "userId": "admin",
  "totalPrice": 0,
  "items": []
}
```

### 2. addToCart
#### POST http://localhost:8094/api/cart?username=...
Request body:
```
{"itemId": "ITEM-001"}
```
Response: 200 OK

### 3. checkout
- This will clear cart, and call Order Service to create order (inventory update) synchronously.
#### POST http://localhost:8094/api/cart/checkout
Response: 200 OK

## Order Service(Authorized, get Order by orderId() requires admin and service)
### 1. createOrder
- This will save an order to Cassandra database and publish an event on order-event topic in kafka.
#### POST http://localhost:8091/api/orders
Request body:
```
{
  "userId": "2b1bc6c0-0d27-41bc-b8ac-db8bbe5ee01b",
  "items": [
    {
      "itemId": "ITEM-001",
      "name": "Chicken Fries - 9 Pc",
      "quantity": 1,
      "price": 4.89
    }
  ],
  "currency": "USD",
  "username": "user"
}
```
Response: 200 
```
[
  {
    "key": {
      "userId": "76662077-c211-4ded-a3ec-d28b2bc6cbcf",
      "createdAt": "2026-02-23T05:11:00.194Z",
      "orderId": "6d9d6091-e283-4dde-9d72-39e6dce86bec"
    },
    "status": "CREATED",
    "total": 26.06,
    "currency": "USD"
  },
  {
    "key": {
      "userId": "76662077-c211-4ded-a3ec-d28b2bc6cbcf",
      "createdAt": "2026-02-19T19:07:45.351Z",
      "orderId": "173a7c99-46e5-4653-b4cd-54330ce40ba7"
    },
    "status": "PENDING",
    "total": 141.2,
    "currency": "USD"
  },
  {
    "key": {
      "userId": "76662077-c211-4ded-a3ec-d28b2bc6cbcf",
      "createdAt": "2026-02-19T19:06:51.260Z",
      "orderId": "92a4f274-9844-4767-a5e5-b89b5ec55a06"
    },
    "status": "CANCELLED",
    "total": 14.61,
    "currency": "USD"
  },
  {
    "key": {
      "userId": "76662077-c211-4ded-a3ec-d28b2bc6cbcf",
      "createdAt": "2026-02-19T19:06:14.139Z",
      "orderId": "d632916d-bea3-4098-9ebf-78e9eb4b47c7"
    },
    "status": "REFUNDED",
    "total": 22.71,
    "currency": "USD"
  }
]
```
### 2. get Order by orderId
Authorization required, only admin and service
#### GET http://localhost:8091/api/orders/{orderId}
Response: 200
```
{
  "orderId": "d632916d-bea3-4098-9ebf-78e9eb4b47c7",
  "userId": "76662077-c211-4ded-a3ec-d28b2bc6cbcf",
  "createdAt": "2026-02-19T19:06:14.139Z",
  "status": "REFUNDED",
  "currency": "USD",
  "items": [
    {
      "itemId": "ITEM-001",
      "name": "Chicken Fries - 9 Pc",
      "quantity": 1,
      "price": 4.89
    },
    {
      "itemId": "ITEM-002",
      "name": "Whopper Meal",
      "quantity": 1,
      "price": 10.59
    }
  ],
  "subtotal": 15.48,
  "tax": 1.24,
  "shippingFee": 5.99,
  "total": 22.71,
  "shippingAddress": "123 S Main St. LA 90000",
  "billingAddress": "123 S Main St. LA 90000",
  "paymentRef": "0f13c373-7979-46c6-92a1-89dd24ace1c5"
}
```
### 3. getOrdersByUser
#### GET http://localhost:8091/api/orders?userId=...
Response: 200
```
[
  {
    "key": {
      "userId": "161b3fce-25c0-4dae-beb3-9f89f00689cb",
      "createdAt": "2026-02-23T04:39:48.487Z",
      "orderId": "2678dcc7-1923-4f4f-b4fb-23e4000587d3"
    },
    "status": "CREATED",
    "total": 11.27,
    "currency": "USD"
  }
]
```
### 4. update an order
#### PATCH http://localhost:8091/api/orders/{orderId}
Request body:
```
{
    "shippingAddress": "4567 Third Ave",
    "billingAddress": "123 Foothill Rd"
}
```
Response: 200
```
{
  "orderId": "767cb9cf-9063-4e9c-9b45-2d9d476d2943",
  "userId": "76662077-c211-4ded-a3ec-d28b2bc6cbcf",
  "createdAt": "2026-02-23T06:54:47.678Z",
  "status": "CREATED",
  "currency": "USD",
  "items": [
    {
      "itemId": "ITEM-003",
      "name": "Impossible™ Whopper",
      "quantity": 1,
      "price": 7.99
    },
    {
      "itemId": "ITEM-007",
      "name": "Spicy Crispy Chicken Sandwich",
      "quantity": 1,
      "price": 3.09
    }
  ],
  "subtotal": 11.08,
  "tax": 0.89,
  "shippingFee": 5.99,
  "total": 17.96,
  "shippingAddress": "4567 Third Ave",
  "billingAddress": "123 Foothill Rd",
  "paymentRef": null
}
```
### 5. cancelOrder 
#### POST http://localhost:8091/api/orders/{orderId}/cancel
Response: 200
```
{
  "orderId": "6d9d6091-e283-4dde-9d72-39e6dce86bec",
  "userId": "76662077-c211-4ded-a3ec-d28b2bc6cbcf",
  "createdAt": "2026-02-23T05:11:00.194Z",
  "status": "CANCELLED",
  "currency": "USD",
  "items": [
    {
      "itemId": "ITEM-002",
      "name": "Whopper Meal",
      "quantity": 1,
      "price": 10.59
    },
    {
      "itemId": "ITEM-003",
      "name": "Impossible™ Whopper",
      "quantity": 1,
      "price": 7.99
    }
  ],
  "subtotal": 18.58,
  "tax": 1.49,
  "shippingFee": 5.99,
  "total": 26.06,
  "shippingAddress": "shipping Rd",
  "billingAddress": "billing Rd",
  "paymentRef": null
}
```
## Payment Service (Authorized, refund needs ADMIN)
### 1. submitPayment
- Use idempotency-key in header
- Publish an event on payment-event kafka topic, the Order Service is consuming this topic and mark orders as PENDING or PAID.
#### POST http://localhost:8091/api/payments
Request body:
```
{
    "orderId":"b39875c8-a105-48f3-bd5e-689af06d2f73",
    "amount": 29.92,
    "currency": "USD",
    "paymentMethod": "credit_card"
}
```
Response: 201
```
{
  "paymentId": "9aba2117-d54d-4de1-b346-3150e501df9c",
  "orderId": "e5a624c8-f3e0-4c3b-8888-8d4d319be086",
  "amount": 29.92,
  "currency": "USD",
  "status": "COMPLETED",
  "paymentMethod": "credit_card",
  "idempotencyKey": "123",
  "createdAt": "2026-02-22T23:17:14.630032",
  "updatedAt": "2026-02-22T23:17:14.630032"
}
```
### 2. getPayment
#### GET http://localhost:8091/api/payments/{paymentId}
Response: 200
```
{
  "paymentId": "9aba2117-d54d-4de1-b346-3150e501df9c",
  "orderId": "e5a624c8-f3e0-4c3b-8888-8d4d319be086",
  "amount": 29.92,
  "currency": "USD",
  "status": "COMPLETED",
  "paymentMethod": "credit_card",
  "idempotencyKey": "123",
  "createdAt": "2026-02-22T23:17:14.630032",
  "updatedAt": "2026-02-22T23:17:14.630032"
}
```
### 3. updatePayment
#### PATCH http://localhost:8091/api/payments/{paymentId}
Request body:
```
{
    "paymentMethod": "paypal"
}
```
Response: 200
```
{
  "paymentId": "ebeb5826-e7d4-431c-98a4-91e83047b6bc",
  "orderId": "fe051e04-1762-49d3-b443-773b74b9cbf2",
  "amount": 141.2,
  "currency": "USD",
  "status": "PENDING",
  "paymentMethod": "ABC",
  "idempotencyKey": "pay-fe051e04-1762-49d3-b443-773b74b9cbf2-1771831268541",
  "createdAt": "2026-02-22T23:21:08.618054",
  "updatedAt": "2026-02-22T23:21:58.7263"
}
```
### 4. refundPayment
#### POST http://localhost:8091/api/payments/{paymentId}/refund
Response: 200
```
{
  "paymentId": "9aba2117-d54d-4de1-b346-3150e501df9c",
  "orderId": "e5a624c8-f3e0-4c3b-8888-8d4d319be086",
  "amount": 29.92,
  "currency": "USD",
  "status": "REFUNDED",
  "paymentMethod": "credit_card",
  "idempotencyKey": "123",
  "createdAt": "2026-02-22T23:17:14.630032",
  "updatedAt": "2026-02-22T23:23:49.37204"
}
```
