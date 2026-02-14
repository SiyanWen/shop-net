import { Button, Card, List, message, Tooltip } from "antd";
import { useEffect, useState } from "react";
import { addItemToCart, getMenus } from "../utils";
import { PlusOutlined } from "@ant-design/icons";

const AddToCartButton = ({ itemId }) => {
  const [loading, setLoading] = useState(false);

  const AddToCart = () => {
    setLoading(true);
    addItemToCart(itemId)
      .then(() => message.success(`Successfully add item`))
      .catch((err) => message.error(err.message))
      .finally(() => {
        setLoading(false);
      });
  };

  return (
    <Tooltip title="Add to shopping cart">
      <Button
        loading={loading}
        type="primary"
        icon={<PlusOutlined />}
        onClick={AddToCart}
      />
    </Tooltip>
  );
};

const FoodList = () => {
  const [foodData, setFoodData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    getMenus()
      .then((data) => {
        setFoodData(data);
      })
      .catch((err) => {
        message.error(err.message);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  return (
    <List
      style={{ marginTop: 20 }}
      loading={loading}
      grid={{
        gutter: 16,
        xs: 1,
        sm: 2,
        md: 4,
        lg: 4,
        xl: 3,
        xxl: 3,
      }}
      dataSource={foodData}
      renderItem={(item) => (
        <List.Item>
          <Card
            title={item.name}
            extra={<AddToCartButton itemId={item.itemId} />}
          >
            {item.pictureUrls && item.pictureUrls.length > 0 && (
              <img
                src={item.pictureUrls[0]}
                alt={item.name}
                style={{ width: "100%", display: "block" }}
              />
            )}
            {`Price: $${item.unitPrice}`}
              <br/>
              {`Qty: ${item.stockQty}`}
          </Card>
        </List.Item>
      )}
    />
  );
};

export default FoodList;
