import React, { useState } from 'react';
import { Routes, Route, Link, useLocation } from 'react-router-dom';
import { BrowserRouter } from 'react-router-dom';
import { Layout, Menu, theme } from 'antd';
import {
  DashboardOutlined,
  TeamOutlined,
  UserOutlined,
  FileTextOutlined,
  EditOutlined,
  CreditCardOutlined,
  BookOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import Dashboard from './pages/Dashboard';
import Authors from './pages/Authors';
import Reviewers from './pages/Reviewers';
import Submissions from './pages/Submissions';
import Reviews from './pages/Reviews';
import Fees from './pages/Fees';
import Publications from './pages/Publications';
import Reports from './pages/Reports';

const { Header, Sider, Content } = Layout;

const menuItems = [
  {
    key: '/',
    icon: <DashboardOutlined />,
    label: <Link to="/">仪表板</Link>,
  },
  {
    key: '/authors',
    icon: <TeamOutlined />,
    label: <Link to="/authors">作者管理</Link>,
  },
  {
    key: '/reviewers',
    icon: <UserOutlined />,
    label: <Link to="/reviewers">审稿人管理</Link>,
  },
  {
    key: '/submissions',
    icon: <FileTextOutlined />,
    label: <Link to="/submissions">投稿管理</Link>,
  },
  {
    key: '/reviews',
    icon: <EditOutlined />,
    label: <Link to="/reviews">审稿管理</Link>,
  },
  {
    key: '/fees',
    icon: <CreditCardOutlined />,
    label: <Link to="/fees">版面费管理</Link>,
  },
  {
    key: '/publications',
    icon: <BookOutlined />,
    label: <Link to="/publications">出版管理</Link>,
  },
  {
    key: '/reports',
    icon: <BarChartOutlined />,
    label: <Link to="/reports">报表管理</Link>,
  },
];

function AppContent() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
        <div className="logo">
          {collapsed ? 'JS' : '期刊投稿系统'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: 0, background: colorBgContainer }}>
          <span className="header-title">学术期刊投稿、审稿、出版管理系统</span>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/authors" element={<Authors />} />
            <Route path="/reviewers" element={<Reviewers />} />
            <Route path="/submissions" element={<Submissions />} />
            <Route path="/reviews" element={<Reviews />} />
            <Route path="/fees" element={<Fees />} />
            <Route path="/publications" element={<Publications />} />
            <Route path="/reports" element={<Reports />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;
