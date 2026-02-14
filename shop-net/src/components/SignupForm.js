import { Button, Checkbox, Form, Input, message, Modal } from "antd";
import React from "react";
import { LockOutlined, UserOutlined, HomeOutlined } from "@ant-design/icons";
import { signup } from "../utils";

class SignupForm extends React.Component {
  state = {
    displayModal: false,
  };

  handleCancel = () => {
    this.setState({
      displayModal: false,
    });
  };

  signupOnClick = () => {
    this.setState({
      displayModal: true,
    });
  };

  onFinish = (data) => {
    signup(data)
      .then(() => {
        this.setState({
          displayModal: false,
        });
        message.success(`Successfully signed up`);
      })
      .catch((err) => {
        message.error(err.message);
      });
  };

  render = () => {
    return (
      <>
        <Button shape="round" type="primary" onClick={this.signupOnClick}>
          Register
        </Button>
        <Modal
          title="Register"
          open={this.state.displayModal}
          onCancel={this.handleCancel}
          footer={null}
          destroyOnClose={true}
        >
          <Form
            name="normal_register"
            initialValues={{ remember: true }}
            onFinish={this.onFinish}
            preserve={false}
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: "Please input your username!" }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Username" />
            </Form.Item>
            <Form.Item
              name="email"
              rules={[{ required: true, message: "Please input your email!" }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Email" />
            </Form.Item>
            <Form.Item
              name="password"
              rules={[
                { required: true, message: "Please input your password!" },
              ]}
            >
              <Input prefix={<LockOutlined />} placeholder="Password" />
            </Form.Item>

            <Form.Item
              name="role"
              label="Role"
            >
              <Checkbox.Group>
                <Checkbox value="ROLE_USER">User</Checkbox>
                <Checkbox value="ROLE_ADMIN">Admin</Checkbox>
              </Checkbox.Group>
            </Form.Item>
            <Form.Item
              name="shippingAddress"
              rules={[{ required: true, message: "Please input your shipping address!" }]}
            >
              <Input prefix={<HomeOutlined />} placeholder="Shipping Address" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit">
                Register
              </Button>
            </Form.Item>
          </Form>
        </Modal>
      </>
    );
  };
}

export default SignupForm;
