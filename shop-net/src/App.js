import { Button, Layout, Typography } from "antd";
import { useEffect, useState } from "react";
import FoodList from "./components/FoodList";
import LoginForm from "./components/LoginForm";
import MyCart from "./components/MyCart";
import MyOrders from "./components/MyOrders";
import OrderLookup from "./components/OrderLookup";
import SignupForm from "./components/SignupForm";
import { getUserInfo } from "./utils";

const { Header, Content } = Layout;
const { Title } = Typography;

function App() {
  const [authed, setAuthed] = useState(!!localStorage.getItem("token"));
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
      console.log("isAdmin:", isAdmin);
    if (!authed) {
      setIsAdmin(false);
      return;
    }
    getUserInfo()
      .then((user) => {
          console.log("roles:",user.roles);
        if (user.roles?.includes("ROLE_ADMIN")) {
          setIsAdmin(true);
        }
      })
      .catch(() => {});
  }, [authed]);

  return (
    <Layout style={{ height: "100vh" }}>
      <Header>
        <div
          className="header"
          style={{ display: "flex", justifyContent: "space-between" }}
        >
          <Title
            level={2}
            style={{ color: "white", lineHeight: "inherit", marginBottom: 0 }}
          >
            Online Order
          </Title>
          <div>{authed ? <><MyCart />{isAdmin && <OrderLookup />}<MyOrders /><Button shape="round" style={{ marginLeft: 8 }} onClick={() => { localStorage.removeItem("token"); localStorage.removeItem("username"); setAuthed(false); }}>Logout</Button></> : <SignupForm />}</div>
        </div>
      </Header>
      <Content
        style={{
          padding: "50px",
          maxHeight: "calc(100% - 64px)",
          overflowY: "auto",
        }}
      >
        {authed ? (
          <FoodList />
        ) : (
          <LoginForm onSuccess={() => setAuthed(true)} />
        )}
      </Content>
    </Layout>
  );
}

export default App;
