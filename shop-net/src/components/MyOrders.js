import { Button, Drawer, List, message, Tag, Typography } from "antd";
import { useEffect, useState } from "react";
import { getOrders, getUserInfo, payOrder } from "../utils";

const { Text } = Typography;

const statusColor = {
  CREATED: "blue",
  PENDING: "orange",
  PAID: "green",
  CANCELLED: "red",
};

const MyOrders = () => {
  const [visible, setVisible] = useState(false);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [payingId, setPayingId] = useState(null);

  useEffect(() => {
    if (!visible) return;

    setLoading(true);
    getUserInfo()
      .then((user) => getOrders(user.id))
      .then((data) => setOrders(data))
      .catch((err) => message.error(err.message))
      .finally(() => setLoading(false));
  }, [visible]);

  const onPay = (order) => {
    const orderId = order.key?.orderId || order.orderId;
    const amount = order.total;
    setPayingId(orderId);
    payOrder(orderId, amount)
      .then(() => {
        message.success("Payment submitted successfully");
        // Refresh orders
        getUserInfo()
          .then((user) => getOrders(user.id))
          .then((data) => setOrders(data));
      })
      .catch((err) => message.error(err.message))
      .finally(() => setPayingId(null));
  };

  return (
    <>
      <Button
        type="primary"
        shape="round"
        onClick={() => setVisible(true)}
        style={{ marginLeft: 8 }}
      >
        Orders
      </Button>
      <Drawer
        title="My Orders"
        onClose={() => setVisible(false)}
        open={visible}
        width={520}
      >
        <List
          loading={loading}
          itemLayout="horizontal"
          dataSource={orders}
          renderItem={(order) => {
            const orderId = order.key?.orderId || order.orderId;
            const createdAt = order.key?.createdAt || order.createdAt;
            return (
              <List.Item
                actions={
                  order.status === "CREATED"
                    ? [
                        <Button
                          type="primary"
                          size="small"
                          loading={payingId === orderId}
                          onClick={() => onPay(order)}
                        >
                          Pay
                        </Button>,
                      ]
                    : []
                }
              >
                <List.Item.Meta
                  title={
                    <span>
                      Order {orderId?.substring(0, 8)}...{" "}
                      <Tag color={statusColor[order.status] || "default"}>
                        {order.status}
                      </Tag>
                    </span>
                  }
                  description={
                    <>
                      <Text>
                        {order.total} {order.currency}
                      </Text>
                      {createdAt && (
                        <Text type="secondary" style={{ marginLeft: 12 }}>
                          {new Date(createdAt).toLocaleString()}
                        </Text>
                      )}
                    </>
                  }
                />
              </List.Item>
            );
          }}
        />
      </Drawer>
    </>
  );
};

export default MyOrders;
