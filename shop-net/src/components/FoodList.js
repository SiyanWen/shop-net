import { Button, Card, List, message, Tooltip } from "antd";
import { useEffect, useState } from "react";
import { addItemToCart, getMenusPaginated } from "../utils";
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

const PAGE_SIZE = 8;

const FoodList = () => {
  const [foodData, setFoodData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [current, setCurrent] = useState(1);
  const [total, setTotal] = useState(0);

  const loadPage = (page) => {
    setLoading(true);
    getMenusPaginated(page - 1, PAGE_SIZE)
      .then((data) => {
        setFoodData(data.content);
        setTotal(data.totalElements);
      })
      .catch((err) => {
        message.error(err.message);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    loadPage(current);
  }, [current]);

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
        xl: 4,
        xxl: 4,
      }}
      dataSource={foodData}
      pagination={{
        current,
        total,
        pageSize: PAGE_SIZE,
        onChange: (page) => setCurrent(page),
      }}
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
