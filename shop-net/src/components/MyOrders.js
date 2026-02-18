import { Button, Drawer, Form, Input, List, message, Modal, Tag, Typography } from "antd";
import { useEffect, useState } from "react";
import { cancelOrder, getOrders, getUserInfo, payOrder, updateOrder } from "../utils";

const { Text } = Typography;

const statusColor = {
  CREATED: "blue",
  PENDING: "orange",
  PAID: "green",
  CANCELLED: "red",
  REFUNDED: "purple",
};

const MyOrders = () => {
  const [visible, setVisible] = useState(false);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [payingId, setPayingId] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);
  const [updatingOrder, setUpdatingOrder] = useState(null);
  const [form] = Form.useForm();

  const refreshOrders = () => {
    return getUserInfo()
      .then((user) => getOrders(user.id))
      .then((data) => setOrders(data));
  };

  useEffect(() => {
    if (!visible) return;

    setLoading(true);
    refreshOrders()
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
        refreshOrders();
      })
      .catch((err) => message.error(err.message))
      .finally(() => setPayingId(null));
  };

  const onCancel = (order) => {
    const orderId = order.key?.orderId || order.orderId;
    setCancellingId(orderId);
    cancelOrder(orderId)
      .then(() => {
        message.success("Order cancelled");
        refreshOrders();
      })
      .catch((err) => message.error(err.message))
      .finally(() => setCancellingId(null));
  };

  const onUpdate = (order) => {
    setUpdatingOrder(order);
    form.setFieldsValue({
      shippingAddress: order.shippingAddress || "",
      billingAddress: order.billingAddress || "",
    });
  };

  const onUpdateSubmit = () => {
    form.validateFields().then((values) => {
      const orderId = updatingOrder.key?.orderId || updatingOrder.orderId;
      updateOrder(orderId, values)
        .then(() => {
          message.success("Order updated");
          setUpdatingOrder(null);
          form.resetFields();
          refreshOrders();
        })
        .catch((err) => message.error(err.message));
    });
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
                        <Button
                          size="small"
                          onClick={() => onUpdate(order)}
                        >
                          Update
                        </Button>,
                        <Button
                          danger
                          size="small"
                          loading={cancellingId === orderId}
                          onClick={() => onCancel(order)}
                        >
                          Cancel
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
      <Modal
        title="Update Order"
        open={!!updatingOrder}
        onOk={onUpdateSubmit}
        onCancel={() => {
          setUpdatingOrder(null);
          form.resetFields();
        }}
        okText="Save"
      >
        <Form form={form} layout="vertical">
          <Form.Item label="Shipping Address" name="shippingAddress">
            <Input />
          </Form.Item>
          <Form.Item label="Billing Address" name="billingAddress">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default MyOrders;
