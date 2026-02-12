// runs once on first start
db = db.getSiblingDB('itemdb');

db.createCollection('items');

db.items.createIndex({ itemId: 1 }, { unique: true });
db.items.createIndex({ upc: 1 }, { unique: true, sparse: true });
db.items.createIndex({ name: "text" });

// seed samples
db.items.insertMany([
  {
    itemId: "ITEM-001",
    name: "Wireless Headphones",
    unitPrice: 199.99,
    pictureUrls: [
      "https://cdn.example.com/items/headphones-front.jpg",
      "https://cdn.example.com/items/headphones-side.jpg"
    ],
    upc: "012345678901",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-002",
    name: "Mechanical Keyboard",
    unitPrice: 89.99,
    pictureUrls: [
      "https://cdn.example.com/items/keyboard-top.jpg"
    ],
    upc: "012345678902",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-003",
    name: "USB-C Hub",
    unitPrice: 45.00,
    pictureUrls: [
      "https://cdn.example.com/items/usb-hub.jpg"
    ],
    upc: "012345678903",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);
