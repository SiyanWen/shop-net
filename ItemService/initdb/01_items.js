// runs once on first start
db = db.getSiblingDB('catalogdb');

db.createCollection('items');

db.items.createIndex({ sku: 1 }, { unique: true });
db.items.createIndex({ upc: 1 }, { unique: true, sparse: true });
db.items.createIndex({ category: 1, status: 1 });
db.items.createIndex({ "price.amount": 1 });
db.items.createIndex({ name: "text", description: "text", tags: 1 });

// seed sample
db.items.insertOne({
  sku: "SKU-12345",
  name: "Wireless Headphones",
  category: "Electronics/Audio/Headphones",
  status: "ACTIVE",
  price: { currency: "USD", amount: 199.99, list: 249.99 },
  attributes: { color: "Black", bluetooth: "5.3" },
  images: [{url: "https://cdn.example/1.jpg", alt: "Front"}],
  createdAt: new Date(),
  updatedAt: new Date()
});
