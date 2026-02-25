import React from 'react'
import ReactDOM from 'react-dom/client'
import { App, Button, Card, Form, Input, InputNumber, Layout, Radio, Space, Typography, message } from 'antd'
import 'antd/dist/reset.css'

const { Header, Content } = Layout

function WatchRulePage() {
  const [ruleForm] = Form.useForm()
  const [commandForm] = Form.useForm()

  const createRule = async (values) => {
    const response = await fetch('http://localhost:8080/api/watch-rules', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values)
    })

    if (!response.ok) {
      message.error('创建盯盘规则失败')
      return
    }

    message.success('盯盘规则已创建')
    ruleForm.resetFields()
  }

  const sendAgentCommand = async (values) => {
    const response = await fetch('http://localhost:8080/api/agent/commands', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values)
    })

    const data = await response.json()
    if (!response.ok || data.parsedIntent === 'UNSUPPORTED') {
      message.warning(data.message || '指令执行失败')
      return
    }

    message.success(data.message || 'OpenClaw 指令执行成功')
    commandForm.resetFields()
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header>
        <Typography.Title style={{ color: '#fff', margin: 0 }} level={3}>A股智能盯盘 MVP（Ant Design）</Typography.Title>
      </Header>
      <Content style={{ padding: 24, maxWidth: 900, margin: '0 auto', width: '100%' }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Card title="创建盯盘提醒规则（手动）">
            <Form
              form={ruleForm}
              layout="vertical"
              onFinish={createRule}
              initialValues={{ triggerType: 'PRICE_BELOW', notifyChannel: 'APP_PUSH' }}
            >
              <Form.Item name="symbol" label="股票代码" rules={[{ required: true, message: '请输入股票代码' }]}>
                <Input placeholder="例如：600519" />
              </Form.Item>
              <Form.Item name="triggerType" label="触发类型" rules={[{ required: true }]}>
                <Radio.Group>
                  <Space>
                    <Radio value="PRICE_BELOW">跌破阈值提醒</Radio>
                    <Radio value="PRICE_ABOVE">突破阈值提醒</Radio>
                  </Space>
                </Radio.Group>
              </Form.Item>
              <Form.Item name="threshold" label="价格阈值" rules={[{ required: true, message: '请输入阈值' }]}>
                <InputNumber style={{ width: '100%' }} min={0.01} precision={2} />
              </Form.Item>
              <Form.Item name="notifyChannel" label="提醒渠道" rules={[{ required: true }]}>
                <Input placeholder="APP_PUSH / EMAIL / IN_APP" />
              </Form.Item>
              <Button type="primary" htmlType="submit">创建规则</Button>
            </Form>
          </Card>

          <Card title="OpenClaw 指令创建规则（多工具编排入口）">
            <Form form={commandForm} layout="vertical" onFinish={sendAgentCommand}>
              <Form.Item name="instruction" label="自然语言指令" rules={[{ required: true, message: '请输入指令' }]}>
                <Input.TextArea rows={3} placeholder="例如：帮我盯住600519，跌破1600提醒我" />
              </Form.Item>
              <Button type="primary" htmlType="submit">发送指令</Button>
            </Form>
          </Card>
        </Space>
      </Content>
    </Layout>
  )
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App>
      <WatchRulePage />
    </App>
  </React.StrictMode>
)
