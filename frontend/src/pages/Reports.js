import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Select, message, Card, Row, Col, Statistic, Descriptions, Space, Tag } from 'antd';
import { PlusOutlined, EyeOutlined, BarChartOutlined, FileTextOutlined, TeamOutlined, BookOutlined } from '@ant-design/icons';
import { reportApi } from '../api';

const { Option } = Select;

function Reports() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [generateModalVisible, setGenerateModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedReport, setSelectedReport] = useState(null);
  const [form] = Form.useForm();

  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;
  const years = Array.from({ length: 5 }, (_, i) => currentYear - i);
  const months = Array.from({ length: 12 }, (_, i) => i + 1);

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const res = await reportApi.getAll();
      setReports(res.data || []);
    } catch (error) {
      message.error('加载报表列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerate = () => {
    form.resetFields();
    form.setFieldsValue({
      year: currentYear,
      month: currentMonth > 1 ? currentMonth - 1 : 12,
    });
    setGenerateModalVisible(true);
  };

  const handleSubmitGenerate = async () => {
    try {
      const values = await form.validateFields();
      const res = await reportApi.generate({
        year: values.year,
        month: values.month,
      });
      message.success('报表生成成功');
      setGenerateModalVisible(false);
      setSelectedReport(res.data);
      setDetailVisible(true);
      fetchReports();
    } catch (error) {
      message.error('生成报表失败');
      console.error(error);
    }
  };

  const handleDetail = (record) => {
    setSelectedReport(record);
    setDetailVisible(true);
  };

  const columns = [
    {
      title: '报表ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '期间',
      dataIndex: 'period',
      key: 'period',
      width: 120,
    },
    {
      title: '总投稿数',
      dataIndex: 'totalSubmissions',
      key: 'totalSubmissions',
      width: 100,
    },
    {
      title: '录用数',
      dataIndex: 'accepted',
      key: 'accepted',
      width: 80,
    },
    {
      title: '拒稿数',
      dataIndex: 'rejected',
      key: 'rejected',
      width: 80,
    },
    {
      title: '录用率',
      dataIndex: 'acceptanceRate',
      key: 'acceptanceRate',
      render: (rate) => `${rate}%`,
      width: 100,
    },
    {
      title: '平均审稿天数',
      dataIndex: 'averageReviewDays',
      key: 'averageReviewDays',
      width: 120,
    },
    {
      title: '版面费总收入',
      dataIndex: 'totalFeesCollected',
      key: 'totalFeesCollected',
      render: (amount) => `¥${amount}`,
      width: 120,
    },
    {
      title: '生成时间',
      dataIndex: 'generatedAt',
      key: 'generatedAt',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => handleDetail(record)}
        >
          详情
        </Button>
      ),
    },
  ];

  const renderReportDetail = (report) => {
    if (!report) return null;

    return (
      <div>
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="总投稿数"
                value={report.totalSubmissions}
                prefix={<FileTextOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="初审通过"
                value={report.initialReviewPassed}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="初审未通过"
                value={report.initialReviewFailed}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="审稿中"
                value={report.inReview}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
        </Row>

        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="需修改"
                value={report.revisionNeeded}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已录用"
                value={report.accepted}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已拒稿"
                value={report.rejected}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已出版"
                value={report.published}
                prefix={<BookOutlined />}
              />
            </Card>
          </Col>
        </Row>

        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="已撤稿"
                value={report.withdrawn}
                valueStyle={{ color: '#8c8c8c' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="录用率"
                value={report.acceptanceRate}
                suffix="%"
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="平均审稿天数"
                value={report.averageReviewDays}
                suffix="天"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="超时审稿"
                value={report.overdueReviews}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
        </Row>

        <Descriptions bordered column={2} title="版面费统计">
          <Descriptions.Item label="总收入">
            <span style={{ fontSize: 18, fontWeight: 'bold', color: '#52c41a' }}>
              ¥{report.totalFeesCollected}
            </span>
          </Descriptions.Item>
          <Descriptions.Item label="缴费笔数">{report.feesCollectedCount}</Descriptions.Item>
        </Descriptions>
      </div>
    );
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleGenerate}>
          生成月报
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={reports}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: '暂无报表数据' }}
      />

      <Modal
        title="生成月度报表"
        open={generateModalVisible}
        onOk={handleSubmitGenerate}
        onCancel={() => setGenerateModalVisible(false)}
        width={400}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="year"
            label="年份"
            rules={[{ required: true, message: '请选择年份' }]}
          >
            <Select placeholder="请选择年份">
              {years.map(y => (
                <Option key={y} value={y}>{y}年</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="month"
            label="月份"
            rules={[{ required: true, message: '请选择月份' }]}
          >
            <Select placeholder="请选择月份">
              {months.map(m => (
                <Option key={m} value={m}>{m}月</Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="报表详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={900}
      >
        {selectedReport && (
          <div>
            <div style={{ marginBottom: 16 }}>
              <Tag color="blue">期间: {selectedReport.period}</Tag>
              <Tag color="default">生成时间: {selectedReport.generatedAt}</Tag>
            </div>
            {renderReportDetail(selectedReport)}
          </div>
        )}
      </Modal>
    </div>
  );
}

export default Reports;
