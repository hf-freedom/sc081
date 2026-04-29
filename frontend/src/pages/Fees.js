import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, InputNumber, message, Tag, Descriptions, Space, Popconfirm } from 'antd';
import { CheckOutlined, EyeOutlined, CreditCardOutlined } from '@ant-design/icons';
import { feeApi, submissionApi, authorApi } from '../api';

const { TextArea } = Input;
const { Option } = Select;

const paymentStatusColors = {
  PENDING: 'orange',
  PAID: 'green',
  REFUNDED: 'default',
};

const paymentStatusDescriptions = {
  PENDING: '待支付',
  PAID: '已支付',
  REFUNDED: '已退款',
};

const paymentMethods = ['银行转账', '支付宝', '微信支付', '信用卡', '现金'];

function Fees() {
  const [fees, setFees] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [payModalVisible, setPayModalVisible] = useState(false);
  const [selectedFee, setSelectedFee] = useState(null);
  const [form] = Form.useForm();
  const [filterStatus, setFilterStatus] = useState(null);

  useEffect(() => {
    fetchData();
  }, [filterStatus]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [feesRes, submissionsRes, authorsRes] = await Promise.all([
        feeApi.getByStatus('PENDING').catch(() => ({ data: [] })),
        submissionApi.getAll(),
        authorApi.getAll(),
      ]);
      setFees(feesRes.data || []);
      setSubmissions(submissionsRes.data || []);
      setAuthors(authorsRes.data || []);
    } catch (error) {
      message.error('加载数据失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleDetail = (record) => {
    setSelectedFee(record);
    setDetailVisible(true);
  };

  const handlePay = (record) => {
    setSelectedFee(record);
    form.setFieldsValue({
      amount: record.totalAmount,
      paymentMethod: '银行转账',
    });
    setPayModalVisible(true);
  };

  const handleSubmitPay = async () => {
    try {
      const values = await form.validateFields();
      await feeApi.pay(selectedFee.id, {
        amount: values.amount,
        paymentMethod: values.paymentMethod,
        transactionId: values.transactionId,
      });
      message.success('支付成功');
      setPayModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleApplyPenalty = async (record) => {
    try {
      await feeApi.applyPenalty(record.id);
      message.success('滞纳金已添加');
      fetchData();
    } catch (error) {
      message.error(error.response?.data?.error || '操作失败');
      console.error(error);
    }
  };

  const columns = [
    {
      title: '费用ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '发票编号',
      dataIndex: 'invoiceNumber',
      key: 'invoiceNumber',
    },
    {
      title: '投稿ID',
      dataIndex: 'submissionId',
      key: 'submissionId',
      width: 80,
    },
    {
      title: '投稿标题',
      key: 'title',
      render: (_, record) => {
        const submission = submissions.find(s => s.id === record.submissionId);
        return submission?.title || '-';
      },
      ellipsis: true,
    },
    {
      title: '作者',
      key: 'author',
      render: (_, record) => {
        const submission = submissions.find(s => s.id === record.submissionId);
        const author = authors.find(a => a.id === submission?.authorId);
        return author?.name || '-';
      },
    },
    {
      title: '总金额',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (amount) => `¥${amount}`,
    },
    {
      title: '已支付',
      dataIndex: 'paidAmount',
      key: 'paidAmount',
      render: (amount) => `¥${amount || 0}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={paymentStatusColors[status] || 'default'}>
          {paymentStatusDescriptions[status] || status}
        </Tag>
      ),
    },
    {
      title: '截止日期',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 180,
      render: (date) => {
        if (!date) return '-';
        const isOverdue = new Date(date) < new Date();
        return (
          <span style={{ color: isOverdue ? 'red' : 'inherit' }}>
            {date} {isOverdue && <Tag color="red">已逾期</Tag>}
          </span>
        );
      },
    },
    {
      title: '支付时间',
      dataIndex: 'paidAt',
      key: 'paidAt',
      width: 180,
      render: (date) => date || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleDetail(record)}
          >
            详情
          </Button>
          {record.status === 'PENDING' && (
            <>
              <Button
                type="link"
                size="small"
                icon={<CheckOutlined />}
                onClick={() => handlePay(record)}
              >
                登记支付
              </Button>
              {record.dueDate && new Date(record.dueDate) < new Date() && (
                <Popconfirm title="确定要添加滞纳金吗？" onConfirm={() => handleApplyPenalty(record)}>
                  <Button type="link" size="small" danger>
                    添加滞纳金
                  </Button>
                </Popconfirm>
              )}
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 8, alignItems: 'center' }}>
        <Select
          style={{ width: 150 }}
          placeholder="筛选状态"
          allowClear
          value={filterStatus}
          onChange={setFilterStatus}
        >
          <Option value="PENDING">待支付</Option>
          <Option value="PAID">已支付</Option>
          <Option value="REFUNDED">已退款</Option>
        </Select>
      </div>

      <Table
        columns={columns}
        dataSource={fees}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: '暂无版面费数据' }}
      />

      <Modal
        title="版面费详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {selectedFee && (
          <div>
            <Descriptions bordered column={2} title="基本信息">
              <Descriptions.Item label="费用ID">{selectedFee.id}</Descriptions.Item>
              <Descriptions.Item label="发票编号">{selectedFee.invoiceNumber}</Descriptions.Item>
              <Descriptions.Item label="投稿ID">{selectedFee.submissionId}</Descriptions.Item>
              <Descriptions.Item label="投稿标题">
                {submissions.find(s => s.id === selectedFee.submissionId)?.title || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">{selectedFee.createdAt}</Descriptions.Item>
              <Descriptions.Item label="截止日期">{selectedFee.dueDate || '-'}</Descriptions.Item>
            </Descriptions>

            <Descriptions bordered column={2} title="费用明细" style={{ marginTop: 16 }}>
              <Descriptions.Item label="基础费用">¥{selectedFee.baseFee || 0}</Descriptions.Item>
              <Descriptions.Item label="超页费用">¥{selectedFee.extraPageFee || 0}</Descriptions.Item>
              <Descriptions.Item label="彩色印刷费">¥{selectedFee.colorFee || 0}</Descriptions.Item>
              <Descriptions.Item label="开放获取费">¥{selectedFee.openAccessFee || 0}</Descriptions.Item>
              <Descriptions.Item label="总金额" span={2}>
                <span style={{ fontSize: 18, fontWeight: 'bold', color: '#1890ff' }}>
                  ¥{selectedFee.totalAmount}
                </span>
              </Descriptions.Item>
            </Descriptions>

            <Descriptions bordered column={2} title="支付信息" style={{ marginTop: 16 }}>
              <Descriptions.Item label="状态">
                <Tag color={paymentStatusColors[selectedFee.status] || 'default'}>
                  {paymentStatusDescriptions[selectedFee.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="已支付金额">¥{selectedFee.paidAmount || 0}</Descriptions.Item>
              <Descriptions.Item label="支付时间">{selectedFee.paidAt || '-'}</Descriptions.Item>
              <Descriptions.Item label="支付方式">{selectedFee.paymentMethod || '-'}</Descriptions.Item>
              <Descriptions.Item label="交易单号" span={2}>
                {selectedFee.transactionId || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>
                {selectedFee.notes || '-'}
              </Descriptions.Item>
            </Descriptions>
          </div>
        )}
      </Modal>

      <Modal
        title="登记支付"
        open={payModalVisible}
        onOk={handleSubmitPay}
        onCancel={() => setPayModalVisible(false)}
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="amount"
            label="支付金额"
            rules={[{ required: true, message: '请输入支付金额' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              min={0}
              precision={2}
              prefix="¥"
              placeholder="请输入支付金额"
            />
          </Form.Item>
          <Form.Item
            name="paymentMethod"
            label="支付方式"
            rules={[{ required: true, message: '请选择支付方式' }]}
          >
            <Select placeholder="请选择支付方式">
              {paymentMethods.map(m => (
                <Option key={m} value={m}>{m}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="transactionId" label="交易单号">
            <Input placeholder="请输入交易单号（可选）" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default Fees;
