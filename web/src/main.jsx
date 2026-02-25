import React from 'react'
import ReactDOM from 'react-dom/client'
import { App, Button, Card, Form, Input, InputNumber, Layout, Radio, Select, Space, Typography, message } from 'antd'
import 'antd/dist/reset.css'

const { Header, Content } = Layout
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

async function fetchJson(url, options) {
  const response = await fetch(url, options)
  const data = await response.json().catch(() => ({}))
  return { response, data }
}

function WatchRulePage() {
  const [ruleForm] = Form.useForm()
  const [commandForm] = Form.useForm()

  const createRule = async (values) => {
    const { response, data } = await fetchJson(`${API_BASE_URL}/api/watch-rules`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values)
    })

    if (!response.ok) {
      message.error(data.message || '创建盯盘规则失败')
      return
    }

    message.success('盯盘规则已创建')
    ruleForm.resetFields()
  }

  const sendAgentCommand = async (values) => {
    const { response, data } = await fetchJson(`${API_BASE_URL}/api/agent/commands`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values)
    })

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
              initialValues={{
                triggerType: 'PRICE_BELOW',
                notifyChannel: 'APP_PUSH',
                coolDownSeconds: 300
              }}
            >
              <Form.Item name="symbol" label="股票代码" rules={[{ required: true, message: '请输入6位股票代码' }, { pattern: /^\d{6}$/, message: '必须为6位数字代码' }]}>
                <Input placeholder="例如：600519" maxLength={6} />
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
                <Select options={[
                  { label: 'APP Push', value: 'APP_PUSH' },
                  { label: '站内信', value: 'IN_APP' },
                  { label: 'Email', value: 'EMAIL' }
                ]} />
              </Form.Item>
              <Form.Item name="coolDownSeconds" label="静默时长（秒）" rules={[{ required: true, message: '请输入静默时长' }]}>
                <InputNumber style={{ width: '100%' }} min={0} max={3600} precision={0} />
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
