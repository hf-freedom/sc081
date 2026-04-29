import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Descriptions, Space, Popconfirm, Radio, InputNumber } from 'antd';
import { CheckOutlined, EyeOutlined, RedoOutlined, SearchOutlined } from '@ant-design/icons';
import { reviewApi, reviewerApi, submissionApi, revisionApi } from '../api';

const { TextArea } = Input;
const { Option } = Select;
const { Group: RadioGroup } = Radio;

const reviewResultOptions = [
  { value: 'ACCEPT', label: '录用' },
  { value: 'MINOR_REVISION', label: '小修' },
  { value: 'MAJOR_REVISION', label: '大修' },
  { value: 'REJECT', label: '拒稿' },
];

function Reviews() {
  const [reviews, setReviews] = useState([]);
  const [reviewers, setReviewers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [completeModalVisible, setCompleteModalVisible] = useState(false);
  const [selectedReview, setSelectedReview] = useState(null);
  const [form] = Form.useForm();
  const [filterStatus, setFilterStatus] = useState(null);

  useEffect(() => {
    fetchData();
  }, [filterStatus]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [reviewsRes, reviewersRes] = await Promise.all([
        reviewApi.getOverdue().catch(() => ({ data: [] })),
        reviewerApi.getAll(),
      ]);
      setReviews(reviewsRes.data || []);
      setReviewers(reviewersRes.data || []);
    } catch (error) {
      message.error('加载数据失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleDetail = (record) => {
    setSelectedReview(record);
    setDetailVisible(true);
  };

  const handleComplete = (record) => {
    setSelectedReview(record);
    form.resetFields();
    setCompleteModalVisible(true);
  };

  const handleSubmitComplete = async () => {
    try {
      const values = await form.validateFields();
      await reviewApi.complete(selectedReview.id, {
        result: values.result,
        comment: values.comment,
        confidentialComment: values.confidentialComment,
        recommendationScore: values.recommendationScore || 0,
      });
      message.success('审稿完成');
      setCompleteModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleReplaceReviewer = (record) => {
    Modal.confirm({
      title: '更换审稿人',
      content: (
        <Form.Item label="新审稿人" required>
          <Select
            id="newReviewerId"
            placeholder="请选择新审稿人"
            style={{ width: 300 }}
            showSearch
            filterOption={(input, option) =>
              option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {reviewers.filter(r => r.active && r.id !== record.reviewerId).map(r => (
              <Option key={r.id} value={r.id}>
                {r.name} ({r.institution})
              </Option>
            ))}
          </Select>
        </Form.Item>
      ),
      onOk: async () => {
        const select = document.getElementById('newReviewerId');
        const newReviewerId = select?.value;
        if (!newReviewerId) {
          message.error('请选择审稿人');
          return Promise.reject();
        }
        try {
          await reviewApi.replaceReviewer(record.id, {
            newReviewerId: parseInt(newReviewerId),
          });
          message.success('审稿人更换成功');
          fetchData();
        } catch (error) {
          message.error('更换失败');
          console.error(error);
          return Promise.reject();
        }
      },
    });
  };

  const columns = [
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
      title: '审稿人',
      dataIndex: 'reviewerId',
      key: 'reviewerId',
      render: (reviewerId) => {
        const reviewer = reviewers.find(r => r.id === reviewerId);
        return reviewer?.name || '-';
      },
    },
    {
      title: '分配时间',
      dataIndex: 'assignedAt',
      key: 'assignedAt',
      width: 180,
    },
    {
      title: '截止日期',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 180,
    },
    {
      title: '完成时间',
      dataIndex: 'completedAt',
      key: 'completedAt',
      width: 180,
    },
    {
      title: '结果',
      dataIndex: 'result',
      key: 'result',
      render: (result) => {
        if (!result) return '-';
        const option = reviewResultOptions.find(o => o.value === result);
        return <Tag>{option?.label || result}</Tag>;
      },
    },
    {
      title: '状态',
      key: 'status',
      render: (_, record) => {
        if (record.status === 'COMPLETED') {
          return <Tag color="green">已完成</Tag>;
        }
        if (record.dueDate && new Date(record.dueDate) < new Date()) {
          return <Tag color="red">已超时</Tag>;
        }
        if (record.reminderSentAt) {
          return <Tag color="orange">已提醒</Tag>;
        }
        return <Tag color="blue">进行中</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
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
          {record.status !== 'COMPLETED' && (
            <Button
              type="link"
              size="small"
              icon={<CheckOutlined />}
              onClick={() => handleComplete(record)}
            >
              完成审稿
            </Button>
          )}
          {record.status !== 'COMPLETED' && (
            <Button
              type="link"
              size="small"
              danger
              onClick={() => handleReplaceReviewer(record)}
            >
              更换审稿人
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 8, alignItems: 'center' }}>
        <Button icon={<RedoOutlined />} onClick={fetchData}>
          刷新
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={reviews}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: '暂无审稿数据' }}
      />

      <Modal
        title="审稿详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {selectedReview && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="审稿ID">{selectedReview.id}</Descriptions.Item>
            <Descriptions.Item label="投稿ID">{selectedReview.submissionId}</Descriptions.Item>
            <Descriptions.Item label="审稿人">
              {reviewers.find(r => r.id === selectedReview.reviewerId)?.name || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="分配时间">{selectedReview.assignedAt}</Descriptions.Item>
            <Descriptions.Item label="截止日期">{selectedReview.dueDate}</Descriptions.Item>
            <Descriptions.Item label="完成时间">{selectedReview.completedAt || '-'}</Descriptions.Item>
            <Descriptions.Item label="提醒时间">{selectedReview.reminderSentAt || '-'}</Descriptions.Item>
            {selectedReview.result && (
              <Descriptions.Item label="审稿结果">
                {reviewResultOptions.find(o => o.value === selectedReview.result)?.label || selectedReview.result}
              </Descriptions.Item>
            )}
            {selectedReview.comment && (
              <Descriptions.Item label="审稿意见">
                {selectedReview.comment}
              </Descriptions.Item>
            )}
            {selectedReview.confidentialComment && (
              <Descriptions.Item label="保密意见">
                {selectedReview.confidentialComment}
              </Descriptions.Item>
            )}
            {selectedReview.recommendationScore !== undefined && (
              <Descriptions.Item label="推荐分数">
                {selectedReview.recommendationScore}
              </Descriptions.Item>
            )}
            {selectedReview.conflictOfInterest && (
              <Descriptions.Item label="利益冲突">
                <Tag color="red">是</Tag>
                {selectedReview.conflictReason && (
                  <div style={{ marginTop: 8 }}>{selectedReview.conflictReason}</div>
                )}
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>

      <Modal
        title="完成审稿"
        open={completeModalVisible}
        onOk={handleSubmitComplete}
        onCancel={() => setCompleteModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="result"
            label="审稿结果"
            rules={[{ required: true, message: '请选择审稿结果' }]}
          >
            <Select placeholder="请选择审稿结果">
              {reviewResultOptions.map(o => (
                <Option key={o.value} value={o.value}>
                  {o.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="comment"
            label="审稿意见（给作者）"
            rules={[{ required: true, message: '请输入审稿意见' }]}
          >
            <TextArea placeholder="请输入审稿意见" rows={4} />
          </Form.Item>
          <Form.Item
            name="confidentialComment"
            label="保密意见（仅编辑可见）"
          >
            <TextArea placeholder="请输入保密意见" rows={3} />
          </Form.Item>
          <Form.Item
            name="recommendationScore"
            label="推荐分数（1-10）"
          >
            <InputNumber min={1} max={10} placeholder="请输入推荐分数" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default Reviews;
