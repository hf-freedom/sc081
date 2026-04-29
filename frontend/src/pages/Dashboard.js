import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, Table, Tag, message } from 'antd';
import {
  FileTextOutlined,
  TeamOutlined,
  UserOutlined,
  BookOutlined,
  RedoOutlined,
} from '@ant-design/icons';
import { submissionApi, authorApi, reviewerApi, publicationApi, reviewApi } from '../api';

const statusColors = {
  SUBMITTED: 'blue',
  INITIAL_REVIEW: 'cyan',
  INITIAL_REVIEW_FAILED: 'red',
  REVIEWING: 'orange',
  REVISION_NEEDED: 'gold',
  REJECTED: 'red',
  ACCEPTED: 'green',
  FEE_PENDING: 'purple',
  FEE_PAID: 'green',
  SCHEDULED: 'cyan',
  PUBLISHED: 'green',
  WITHDRAWN: 'default',
};

const statusDescriptions = {
  SUBMITTED: '已投稿',
  INITIAL_REVIEW: '初审中',
  INITIAL_REVIEW_FAILED: '初审未通过',
  REVIEWING: '审稿中',
  REVISION_NEEDED: '需修改',
  REJECTED: '拒稿',
  ACCEPTED: '已录用',
  FEE_PENDING: '待缴费',
  FEE_PAID: '已缴费',
  SCHEDULED: '排期中',
  PUBLISHED: '已出版',
  WITHDRAWN: '已撤稿',
};

function Dashboard() {
  const [stats, setStats] = useState({
    totalSubmissions: 0,
    totalAuthors: 0,
    totalReviewers: 0,
    totalPublications: 0,
  });
  
  const [recentSubmissions, setRecentSubmissions] = useState([]);
  const [overdueReviews, setOverdueReviews] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [submissionsRes, authorsRes, reviewersRes, publicationsRes, overdueRes] = await Promise.all([
        submissionApi.getAll(),
        authorApi.getAll(),
        reviewerApi.getAll(),
        publicationApi.getAll(),
        reviewApi.getOverdue(),
      ]);

      setStats({
        totalSubmissions: submissionsRes.data?.length || 0,
        totalAuthors: authorsRes.data?.length || 0,
        totalReviewers: reviewersRes.data?.length || 0,
        totalPublications: publicationsRes.data?.length || 0,
      });

      const submissions = submissionsRes.data || [];
      setRecentSubmissions(submissions.slice(-10).reverse());
      setOverdueReviews(overdueRes.data || []);
    } catch (error) {
      message.error('加载数据失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const submissionColumns = [
    {
      title: '投稿ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={statusColors[status] || 'default'}>
          {statusDescriptions[status] || status}
        </Tag>
      ),
    },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      width: 180,
    },
  ];

  const overdueColumns = [
    {
      title: '审稿ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '投稿ID',
      dataIndex: 'submissionId',
      key: 'submissionId',
      width: 80,
    },
    {
      title: '截止日期',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 180,
    },
    {
      title: '状态',
      key: 'status',
      render: () => <Tag color="red">已超时</Tag>,
    },
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总投稿数"
              value={stats.totalSubmissions}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="作者总数"
              value={stats.totalAuthors}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="审稿人总数"
              value={stats.totalReviewers}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已出版数"
              value={stats.totalPublications}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={16}>
          <Card 
            title="最近投稿" 
            loading={loading}
            extra={<RedoOutlined onClick={fetchData} style={{ cursor: 'pointer' }} />}
          >
            <Table
              columns={submissionColumns}
              dataSource={recentSubmissions}
              rowKey="id"
              size="small"
              pagination={false}
              locale={{ emptyText: '暂无投稿记录' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card title="超时审稿" loading={loading}>
            <Table
              columns={overdueColumns}
              dataSource={overdueReviews}
              rowKey="id"
              size="small"
              pagination={false}
              locale={{ emptyText: '暂无超时审稿' }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default Dashboard;
