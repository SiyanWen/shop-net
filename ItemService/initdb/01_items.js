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
  },
  {
    itemId: "ITEM-010",
    name: "Classic OREO® Shake",
    unitPrice: 3.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/c3ad483f-bad7-44f1-96af-4c3dcfc63c6d-retina-large.jpg'
    ],
    upc: "012345678910",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-011",
    name: "Original Soft Tofu",
    unitPrice: 17.06,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/b7055ca9-3caf-4d9d-9c99-04be1e36dbbf-retina-large-jpeg'
    ],
    upc: "012345678911",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-012",
    name: "Combination Soft Tofu",
    unitPrice: 17.06,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/37ad1974-1395-4e5c-86ff-fdf120cf8c58-retina-large-jpeg'
    ],
    upc: "012345678912",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-013",
    name: "Seafood Soft Tofu",
    unitPrice: 17.06,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/96bc8289-1950-4b4f-823d-12f33349a5fe-retina-large-jpeg'
    ],
    upc: "012345678913",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-014",
    name: "Seafood Pancake",
    unitPrice: 20.27,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/0a94b7e9-903d-49b7-937a-7940c8b56ad5-retina-large-jpeg'
    ],
    upc: "012345678914",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-015",
    name: "Kimchi Soft Tofu",
    unitPrice: 17.06,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/0c062cff-1868-40e1-946d-29d3e46f1541-retina-large-jpeg'
    ],
    upc: "012345678915",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-016",
    name: "Beef Short Ribs",
    unitPrice: 29.36,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/6340c369-2485-4d60-afcf-ca9068448d84-retina-large.jpg'
    ],
    upc: "012345678916",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-017",
    name: "Dumpling Soft Tofu",
    unitPrice: 17.06,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/b7055ca9-3caf-4d9d-9c99-04be1e36dbbf-retina-large-jpeg'
    ],
    upc: "012345678917",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-018",
    name: "Assorted Mushroom Tofu",
    unitPrice: 17.06,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/b7055ca9-3caf-4d9d-9c99-04be1e36dbbf-retina-large-jpeg'
    ],
    upc: "012345678918",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-019",
    name: "BBQ Beef & Vegetables in Stoneware",
    unitPrice: 20.27,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/9844dd4e-3c74-4942-8f90-2b3f4be25049-retina-large-jpeg'
    ],
    upc: "012345678919",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-020",
    name: "Ham & Cheese Soft Tofu",
    unitPrice: 17.06,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/9c6b2a1c-1e2c-4d80-a111-2bebbcadd64c-retina-large.jpg'
    ],
    upc: "012345678920",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-021",
    name: "Stir Fried Pork with Pepper",
    unitPrice: 13.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/5b34852e-d253-461c-8be8-1bb0bc5e39be-retina-large.jpg'
    ],
    upc: "012345678921",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-022",
    name: "Eggplant with Minced Pork, Garlic, Cilantro",
    unitPrice: 14.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/bf70f262-0c55-41e1-89bc-84c061ae485f-retina-large.jpg'
    ],
    upc: "012345678922",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-023",
    name: "Stir Fried Cauliflower with Pork",
    unitPrice: 14.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/cb870c77-ace1-49ec-aa2f-9e18de102242-retina-large.jpg'
    ],
    upc: "012345678923",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-024",
    name: "Poached Fish Fillets in Sour Soup",
    unitPrice: 17.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/1acf9c6b-189d-4583-a151-7ef522c283d9-retina-large.jpg'
    ],
    upc: "012345678924",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-025",
    name: "Stir Fried Beef with Pepper",
    unitPrice: 16.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/7f05859d-5e83-476d-a45a-73a3eb8a94e0-retina-large.jpg'
    ],
    upc: "012345678925",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-026",
    name: "Stir Fried Shredded Tripe with Wugang Tofu",
    unitPrice: 19.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/8b2ca9fc-2c1d-4bf2-96ff-d0bd3c415e8d-retina-large.jpg'
    ],
    upc: "012345678926",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-027",
    name: "Poached Sliced Beef in Hot Chili Oil",
    unitPrice: 17.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/89ad8679-346e-41d8-b98f-3501fff4b277-retina-large.jpg'
    ],
    upc: "012345678927",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-028",
    name: "Fried Rice",
    unitPrice: 9.50,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/ec06c431-9426-4971-a129-920440e1c9ce-retina-large.jpg'
    ],
    upc: "012345678928",
    stockQty: 120,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-029",
    name: "Smashed Green Pepper, Chinese Eggplant & Preserved Egg",
    unitPrice: 11.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/2fe1b87f-d41f-4fa4-8cae-5f2ee5bb97e4-retina-large.jpg'
    ],
    upc: "012345678929",
    stockQty: 50,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    itemId: "ITEM-030",
    name: "Stir Fried A-Choy with Minced Garlic",
    unitPrice: 10.99,
    pictureUrls: [
      'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/a307e73d-dd12-4841-be14-6f5825a64c59-retina-large.jpg'
    ],
    upc: "012345678930",
    stockQty: 200,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);
