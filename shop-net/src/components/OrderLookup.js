import { Button, Drawer, Input, List, message, Tag, Typography } from "antd";
import { useState } from "react";
import { getOrderById } from "../utils";

const { Text } = Typography;
const { Search } = Input;

const statusColor = {
  CREATED: "blue",
  PENDING: "orange",
  PAID: "green",
  CANCELLED: "red",
};

const OrderLookup = () => {
  const [visible, setVisible] = useState(false);
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);

  const onSearch = (orderId) => {
    if (!orderId?.trim()) {
      message.warning("Please enter an order ID");
      return;
    }
    setLoading(true);
    setOrder(null);
    getOrderById(orderId.trim())
      .then((data) => setOrder(data))
      .catch((err) => message.error(err.message))
      .finally(() => setLoading(false));
  };

  return (
    <>
      <Button
        type="primary"
        shape="round"
        onClick={() => setVisible(true)}
        style={{ marginLeft: 8 }}
      >
        Look up order
      </Button>
      <Drawer
        title="Look Up Order"
        onClose={() => {
          setVisible(false);
          setOrder(null);
        }}
        open={visible}
        width={520}
      >
        <Search
          placeholder="Enter order ID"
          onSearch={onSearch}
          enterButton="Search"
          loading={loading}
          allowClear
          style={{ marginBottom: 16 }}
        />
        {order && (
          <List itemLayout="horizontal">
            <List.Item>
              <List.Item.Meta
                title={
                  <span>
                    Order {order.orderId?.substring(0, 8)}...{" "}
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
                    {order.createdAt && (
                      <Text type="secondary" style={{ marginLeft: 12 }}>
                        {new Date(order.createdAt).toLocaleString()}
                      </Text>
                    )}
                  </>
                }
              />
            </List.Item>
            {order.items?.map((item, idx) => (
              <List.Item key={idx}>
                <List.Item.Meta
                  title={item.name}
                  description={`$${item.price} x ${item.quantity}`}
                />
              </List.Item>
            ))}
          </List>
        )}
      </Drawer>
    </>
  );
};

export default OrderLookup;
