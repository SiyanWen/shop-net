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
    name: "Chicken Fries - 9 Pc",
    unitPrice: 4.89,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f439436f-c5ab-47af-bac4-7b73ab60a24b-retina-large.jpg'
    ],
    upc: "012345678901",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-002",
    name: "Whopper Meal",
    unitPrice: 10.59,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f878a689-618b-4c70-a00f-e7b1f320adc9-retina-large.jpg'
    ],
    upc: "012345678902",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-003",
    name: "Impossible™ Whopper",
    unitPrice: 7.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/5c306a5f-fdd2-41d2-a660-9762aaa8eee8-retina-large.jpg'
    ],
    upc: "012345678903",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-004",
    name: "HERSHEYS® Sundae Pie",
    unitPrice: 3.09,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/80b1670d-e9c0-4886-a5b7-1ad48edd24ca-retina-large.jpg'
    ],
    upc: "012345678904",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-005",
    name: "Whopper",
    unitPrice: 6.39,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/9b3d7985-e457-43b3-938d-5184f48c2687-retina-large-jpeg'
    ],
    upc: "012345678905",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-006",
    name: "Double Whopper Meal",
    unitPrice: 11.69,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/45addf4a-e8a8-47cb-a705-cce1d10ce86d-retina-large.jpg'
    ],
    upc: "012345678906",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-007",
    name: "Spicy Crispy Chicken Sandwich",
    unitPrice: 3.09,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/31dd68c2-06ec-42ad-bcd4-da7bd3425437-retina-large-jpeg'
    ],
    upc: "012345678907",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-008",
    name: "Whopper",
    unitPrice: 6.09,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/3e437f54-fa4e-4e9d-bf80-8a1e5b120f32-retina-large-jpeg'
    ],
    upc: "012345678908",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-009",
    name: "Bacon King Sandwich Meal",
    unitPrice: 125.19,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/adb96c32-3c5b-4375-ba92-b30767d2513d-retina-large.jpg'
    ],
    upc: "012345678909",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);
