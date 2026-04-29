import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, InputNumber, Switch, message, Tag, Descriptions, Space, Popconfirm, Radio } from 'antd';
import { PlusOutlined, EyeOutlined, EditOutlined, CheckOutlined, CloseOutlined, UndoOutlined } from '@ant-design/icons';
import { submissionApi, authorApi, journalApi, reviewApi, revisionApi, feeApi, publicationApi } from '../api';

const { TextArea } = Input;
const { Option } = Select;
const { Group: RadioGroup } = Radio;

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

function Submissions() {
  const [submissions, setSubmissions] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [initialReviewModalVisible, setInitialReviewModalVisible] = useState(false);
  const [selectedSubmission, setSelectedSubmission] = useState(null);
  const [form] = Form.useForm();
  const [initialReviewForm] = Form.useForm();
  const [detailData, setDetailData] = useState(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      console.log('开始加载数据...');
      
      // 获取投稿列表
      try {
        const submissionsRes = await submissionApi.getAll();
        console.log('获取投稿列表响应:', submissionsRes);
        if (submissionsRes.data && Array.isArray(submissionsRes.data)) {
          console.log('投稿列表数据:', submissionsRes.data);
          setSubmissions(submissionsRes.data);
        } else {
          console.warn('投稿列表返回数据格式异常:', submissionsRes.data);
          // 不更新，保留旧数据
        }
      } catch (subError) {
        console.error('获取投稿列表失败:', subError);
        message.error('获取投稿列表失败: ' + (subError.message || '未知错误'));
      }
      
      // 获取作者列表
      try {
        const authorsRes = await authorApi.getAll();
        if (authorsRes.data && Array.isArray(authorsRes.data)) {
          setAuthors(authorsRes.data);
        }
      } catch (authError) {
        console.error('获取作者列表失败:', authError);
      }
      
      // 获取栏目列表
      try {
        const sectionsRes = await journalApi.getActiveSections();
        if (sectionsRes.data && Array.isArray(sectionsRes.data)) {
          setSections(sectionsRes.data);
        }
      } catch (sectError) {
        console.error('获取栏目列表失败:', sectError);
      }
      
    } catch (error) {
      message.error('加载数据失败: ' + (error.message || '未知错误'));
      console.error('fetchData 错误:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    form.resetFields();
    setCreateModalVisible(true);
  };

  const handleDetail = async (record) => {
    setSelectedSubmission(record);
    setLoading(true);
    try {
      const [submissionRes, reviewsRes, revisionsRes, feeRes, publicationRes] = await Promise.all([
        submissionApi.getById(record.id),
        reviewApi.getBySubmissionId(record.id),
        revisionApi.getBySubmissionId(record.id),
        feeApi.getBySubmissionId(record.id).catch(() => ({ data: null })),
        publicationApi.getBySubmissionId(record.id).catch(() => ({ data: null })),
      ]);

      setDetailData({
        submission: submissionRes.data,
        reviews: reviewsRes.data,
        revisions: revisionsRes.data,
        fee: feeRes.data,
        publication: publicationRes.data,
        author: authors.find(a => a.id === submissionRes.data?.authorId),
      });
      setDetailVisible(true);
    } catch (error) {
      message.error('加载详情失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await submissionApi.create(values);
      message.success('投稿成功');
      setCreateModalVisible(false);
      fetchData();
    } catch (error) {
      message.error(error.response?.data?.error || '投稿失败');
      console.error(error);
    }
  };

  const handleStartInitialReview = async (record) => {
    try {
      await submissionApi.startInitialReview(record.id);
      message.success('已开始初审');
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleInitialReview = (record) => {
    setSelectedSubmission(record);
    initialReviewForm.resetFields();
    setInitialReviewModalVisible(true);
  };

  const handleSubmitInitialReview = async () => {
    try {
      const values = await initialReviewForm.validateFields();
      console.log('提交初审数据:', {
        id: selectedSubmission.id,
        passed: values.passed,
        comment: values.comment,
      });
      
      const response = await submissionApi.completeInitialReview(selectedSubmission.id, {
        passed: values.passed,
        comment: values.comment,
      });
      
      console.log('初审完成响应:', response.data);
      message.success('初审完成');
      setInitialReviewModalVisible(false);
      fetchData();
    } catch (error) {
      console.error('初审失败错误:', error);
      const errorMessage = error.response?.data?.error || error.message || '操作失败';
      message.error('初审失败: ' + errorMessage);
    }
  };

  const handleWithdraw = async (record) => {
    Modal.confirm({
      title: '撤稿确认',
      content: (
        <Form.Item label="撤稿原因" required>
          <TextArea
            id="withdrawReason"
            placeholder="请输入撤稿原因"
            rows={3}
          />
        </Form.Item>
      ),
      onOk: async () => {
        const reason = document.getElementById('withdrawReason')?.value;
        try {
          await submissionApi.withdraw(record.id, { reason: reason || '未提供原因' });
          message.success('撤稿成功');
          fetchData();
        } catch (error) {
          message.error(error.response?.data?.error || '撤稿失败');
        }
      },
    });
  };

  const handlePublish = async (record) => {
    try {
      await publicationApi.publish({ submissionId: record.id });
      message.success('出版成功');
      fetchData();
    } catch (error) {
      message.error(error.response?.data?.error || '出版失败');
      console.error(error);
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      width: 200,
    },
    {
      title: '作者',
      dataIndex: 'authorId',
      key: 'authorId',
      render: (authorId) => {
        const author = authors.find(a => a.id === authorId);
        return author?.name || '-';
      },
    },
    {
      title: '页数',
      dataIndex: 'pageCount',
      key: 'pageCount',
      width: 80,
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
    {
      title: '操作',
      key: 'action',
      width: 280,
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
          {record.status === 'SUBMITTED' && (
            <Button
              type="link"
              size="small"
              onClick={() => handleStartInitialReview(record)}
            >
              开始初审
            </Button>
          )}
          {record.status === 'INITIAL_REVIEW' && (
            <Button
              type="link"
              size="small"
              onClick={() => handleInitialReview(record)}
            >
              初审处理
            </Button>
          )}
          {record.status === 'SCHEDULED' && (
            <Popconfirm title="确定要出版吗？" onConfirm={() => handlePublish(record)}>
              <Button type="link" size="small">
                出版
              </Button>
            </Popconfirm>
          )}
          {!['PUBLISHED', 'WITHDRAWN', 'REJECTED', 'INITIAL_REVIEW_FAILED'].includes(record.status) && (
            <Button
              type="link"
              size="small"
              danger
              onClick={() => handleWithdraw(record)}
            >
              撤稿
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新增投稿
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={submissions}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: '暂无投稿数据' }}
      />

      <Modal
        title="新增投稿"
        open={createModalVisible}
        onOk={handleSubmit}
        onCancel={() => setCreateModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input placeholder="请输入标题" />
          </Form.Item>
          <Form.Item
            name="abstracts"
            label="摘要"
            rules={[{ required: true, message: '请输入摘要' }]}
          >
            <TextArea placeholder="请输入摘要" rows={4} />
          </Form.Item>
          <Form.Item name="keywords" label="关键词">
            <Input placeholder="多个关键词用逗号分隔" />
          </Form.Item>
          <Form.Item
            name="authorId"
            label="作者"
            rules={[{ required: true, message: '请选择作者' }]}
          >
            <Select placeholder="请选择作者">
              {authors.map(a => (
                <Option key={a.id} value={a.id} disabled={a.blacklisted}>
                  {a.name} ({a.institution}) {a.blacklisted && <Tag color="red">黑名单</Tag>}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="sectionId"
            label="栏目"
            rules={[{ required: true, message: '请选择栏目' }]}
          >
            <Select placeholder="请选择栏目">
              {sections.map(s => (
                <Option key={s.id} value={s.id}>
                  {s.name} ({s.minPages}-{s.maxPages}页)
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="pageCount"
            label="页数"
            rules={[{ required: true, message: '请输入页数' }]}
          >
            <InputNumber min={1} max={100} placeholder="请输入页数" />
          </Form.Item>
          <Form.Item name="colorPrint" valuePropName="checked" label="彩色印刷">
            <Switch />
          </Form.Item>
          <Form.Item name="openAccess" valuePropName="checked" label="开放获取">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="初审处理"
        open={initialReviewModalVisible}
        onOk={handleSubmitInitialReview}
        onCancel={() => setInitialReviewModalVisible(false)}
        width={500}
      >
        <Form form={initialReviewForm} layout="vertical">
          <Form.Item
            name="passed"
            label="初审结果"
            rules={[{ required: true, message: '请选择初审结果' }]}
          >
            <RadioGroup>
              <Radio value={true}>通过</Radio>
              <Radio value={false}>不通过</Radio>
            </RadioGroup>
          </Form.Item>
          <Form.Item name="comment" label="初审意见">
            <TextArea placeholder="请输入初审意见" rows={4} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="投稿详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={900}
      >
        {detailData && (
          <div>
            <Descriptions bordered column={2} title="基本信息">
              <Descriptions.Item label="ID">{detailData.submission.id}</Descriptions.Item>
              <Descriptions.Item label="标题">{detailData.submission.title}</Descriptions.Item>
              <Descriptions.Item label="作者">{detailData.author?.name || '-'}</Descriptions.Item>
              <Descriptions.Item label="栏目">
                {sections.find(s => s.id === detailData.submission.sectionId)?.name || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="页数">{detailData.submission.pageCount}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColors[detailData.submission.status] || 'default'}>
                  {statusDescriptions[detailData.submission.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="彩色印刷">
                {detailData.submission.colorPrint ? '是' : '否'}
              </Descriptions.Item>
              <Descriptions.Item label="开放获取">
                {detailData.submission.openAccess ? '是' : '否'}
              </Descriptions.Item>
              <Descriptions.Item label="摘要" span={2}>
                {detailData.submission.abstracts}
              </Descriptions.Item>
              <Descriptions.Item label="关键词">{detailData.submission.keywords || '-'}</Descriptions.Item>
              <Descriptions.Item label="提交时间">{detailData.submission.submittedAt}</Descriptions.Item>
            </Descriptions>

            {detailData.reviews?.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <h4>审稿记录</h4>
                <Table
                  dataSource={detailData.reviews}
                  rowKey="id"
                  size="small"
                  pagination={false}
                  columns={[
                    { title: '审稿ID', dataIndex: 'id', width: 80 },
                    { title: '审稿人ID', dataIndex: 'reviewerId', width: 100 },
                    { title: '分配时间', dataIndex: 'assignedAt', width: 180 },
                    { title: '截止日期', dataIndex: 'dueDate', width: 180 },
                    { title: '完成时间', dataIndex: 'completedAt', width: 180 },
                    { title: '结果', dataIndex: 'result', render: (r) => r || '待完成' },
                    { title: '状态', dataIndex: 'status' },
                  ]}
                />
              </div>
            )}

            {detailData.revisions?.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <h4>修改记录</h4>
                <Table
                  dataSource={detailData.revisions}
                  rowKey="id"
                  size="small"
                  pagination={false}
                  columns={[
                    { title: '修改次数', dataIndex: 'revisionNumber', width: 80 },
                    { title: '修改类型', dataIndex: 'isMinor', render: (m) => m ? '小修' : '大修', width: 80 },
                    { title: '请求时间', dataIndex: 'requestedAt', width: 180 },
                    { title: '截止日期', dataIndex: 'dueDate', width: 180 },
                    { title: '提交时间', dataIndex: 'submittedAt', width: 180 },
                    { title: '状态', dataIndex: 'status' },
                  ]}
                />
              </div>
            )}

            {detailData.fee && (
              <div style={{ marginTop: 16 }}>
                <h4>版面费信息</h4>
                <Descriptions bordered column={2} size="small">
                  <Descriptions.Item label="发票编号">{detailData.fee.invoiceNumber}</Descriptions.Item>
                  <Descriptions.Item label="总金额">{detailData.fee.totalAmount}</Descriptions.Item>
                  <Descriptions.Item label="已支付">{detailData.fee.paidAmount}</Descriptions.Item>
                  <Descriptions.Item label="状态">
                    <Tag color={detailData.fee.status === 'PAID' ? 'green' : 'orange'}>
                      {detailData.fee.status === 'PAID' ? '已支付' : '待支付'}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="支付时间">{detailData.fee.paidAt || '-'}</Descriptions.Item>
                  <Descriptions.Item label="支付方式">{detailData.fee.paymentMethod || '-'}</Descriptions.Item>
                </Descriptions>
              </div>
            )}

            {detailData.publication && (
              <div style={{ marginTop: 16 }}>
                <h4>出版信息</h4>
                <Descriptions bordered column={2} size="small">
                  <Descriptions.Item label="DOI">{detailData.publication.doi}</Descriptions.Item>
                  <Descriptions.Item label="卷期">{detailData.publication.volume}卷 {detailData.publication.issue}期</Descriptions.Item>
                  <Descriptions.Item label="页码">{detailData.publication.startPage}-{detailData.publication.endPage}</Descriptions.Item>
                  <Descriptions.Item label="出版日期">{detailData.publication.publicationDate}</Descriptions.Item>
                </Descriptions>
              </div>
            )}

            {detailData.submission.status === 'WITHDRAWN' && (
              <div style={{ marginTop: 16 }}>
                <h4>撤稿信息</h4>
                <Descriptions bordered column={2} size="small">
                  <Descriptions.Item label="撤稿时间">{detailData.submission.withdrawnAt}</Descriptions.Item>
                  <Descriptions.Item label="撤稿原因">{detailData.submission.withdrawReason}</Descriptions.Item>
                  <Descriptions.Item label="处理费">{detailData.submission.withdrawFee || 0}</Descriptions.Item>
                </Descriptions>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}

export default Submissions;
