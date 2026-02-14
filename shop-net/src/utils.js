const getToken = () => localStorage.getItem("token");
const getUserId = () => localStorage.getItem("userId");

export const login = (credentials) => {
  return fetch("/api/v1/auth/jwt/signin", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      accountOrEmail: credentials.username,
      password: credentials.password,
    }),
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to log in");
    }
    return response.json();
  }).then((data) => {
    localStorage.setItem("token", data.accessToken);
    localStorage.setItem("userId", credentials.username);
  });
};

export const signup = (data) => {
  return fetch("/api/v1/auth/jwt/signup", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      username: data.username,
      email: data.email,
      password: data.password,
      role: data.role || [],
      shippingAddress: data.shippingAddress,
    }),
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to sign up");
    }
  });
};

export const getMenus = () => {
  return fetch("/api/items", {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to get items");
    }
    return response.json();
  });
};

export const getCart = () => {
  return fetch(`/api/cart?userId=${getUserId()}`, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to get shopping cart data");
    }
    return response.json();
  });
};

export const checkout = () => {
  return fetch(`/api/cart/checkout?userId=${getUserId()}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getToken()}`,
    },
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to checkout");
    }
  });
};

export const getUserInfo = () => {
  return fetch(`/api/v1/auth/jwt/user?username=${getUserId()}`, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to get user info");
    }
    return response.json();
  });
};

export const getOrders = (userUuid) => {
  return fetch(`/api/orders?userId=${userUuid}`, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to get orders");
    }
    return response.json();
  });
};

export const payOrder = (orderId, amount) => {
  const idempotencyKey = `pay-${orderId}-${Date.now()}`;
  return fetch("/api/payments", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getToken()}`,
      "Idempotency-Key": idempotencyKey,
    },
    body: JSON.stringify({
      orderId,
      amount,
      currency: "USD",
    }),
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to submit payment");
    }
    return response.json();
  });
};

export const addItemToCart = (itemId) => {
  return fetch(`/api/cart?userId=${getUserId()}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getToken()}`,
    },
    body: JSON.stringify({ itemId }),
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw Error("Fail to add item to shopping cart");
    }
  });
};
