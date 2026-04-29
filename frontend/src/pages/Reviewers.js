import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Switch, message, Tag, Descriptions, Select } from 'antd';
import { PlusOutlined, EditOutlined, EyeOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { reviewerApi } from '../api';

const { TextArea } = Input;
const { Option } = Select;

function Reviewers() {
  const [reviewers, setReviewers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedReviewer, setSelectedReviewer] = useState(null);
  const [form] = Form.useForm();
  const [isEdit, setIsEdit] = useState(false);

  useEffect(() => {
    fetchReviewers();
  }, []);

  const fetchReviewers = async () => {
    setLoading(true);
    try {
      const res = await reviewerApi.getAll();
      setReviewers(res.data || []);
    } catch (error) {
      message.error('加载审稿人列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setIsEdit(false);
    form.resetFields();
    setSelectedReviewer(null);
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setIsEdit(true);
    setSelectedReviewer(record);
    form.setFieldsValue({
      ...record,
      conflictOfInterestInstitutions: record.conflictOfInterestInstitutions?.join(','),
    });
    setModalVisible(true);
  };

  const handleDetail = (record) => {
    setSelectedReviewer(record);
    setDetailVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (values.conflictOfInterestInstitutions) {
        values.conflictOfInterestInstitutions = values.conflictOfInterestInstitutions
          .split(',')
          .map(s => s.trim())
          .filter(s => s);
      } else {
        values.conflictOfInterestInstitutions = [];
      }
      
      if (isEdit && selectedReviewer) {
        await reviewerApi.update(selectedReviewer.id, values);
        message.success('更新成功');
      } else {
        await reviewerApi.create(values);
        message.success('创建成功');
      }
      
      setModalVisible(false);
      fetchReviewers();
    } catch (error) {
      message.error(isEdit ? '更新失败' : '创建失败');
      console.error(error);
    }
  };

  const handleActive = async (record, active) => {
    try {
      await reviewerApi.setActive(record.id, { active });
      message.success(active ? '已启用' : '已禁用');
      fetchReviewers();
    } catch (error) {
      message.error('操作失败');
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
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '机构',
      dataIndex: 'institution',
      key: 'institution',
      ellipsis: true,
    },
    {
      title: '研究领域',
      dataIndex: 'researchField',
      key: 'researchField',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'active',
      key: 'active',
      render: (active) => (
        <Tag color={active ? 'green' : 'red'}>
          {active ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 240,
      render: (_, record) => (
        <>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handleDetail(record)}
          >
            详情
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          {record.active ? (
            <Button
              type="link"
              danger
              icon={<CloseOutlined />}
              onClick={() => handleActive(record, false)}
            >
              禁用
            </Button>
          ) : (
            <Button
              type="link"
              icon={<CheckOutlined />}
              onClick={() => handleActive(record, true)}
            >
              启用
            </Button>
          )}
        </>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加审稿人
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={reviewers}
        rowKey="id"
        loading={loading}
        locale={{ emptyText: '暂无审稿人数据' }}
      />

      <Modal
        title={isEdit ? '编辑审稿人' : '添加审稿人'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="姓名"
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '请输入有效的邮箱地址' },
            ]}
          >
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item name="phone" label="电话">
            <Input placeholder="请输入电话" />
          </Form.Item>
          <Form.Item name="institution" label="机构">
            <Input placeholder="请输入机构" />
          </Form.Item>
          <Form.Item name="researchField" label="研究领域">
            <Input placeholder="请输入研究领域" />
          </Form.Item>
          <Form.Item
            name="conflictOfInterestInstitutions"
            label="利益冲突机构"
          >
            <TextArea
              placeholder="多个机构用逗号分隔"
              rows={2}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="审稿人详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {selectedReviewer && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="ID">{selectedReviewer.id}</Descriptions.Item>
            <Descriptions.Item label="姓名">{selectedReviewer.name}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{selectedReviewer.email}</Descriptions.Item>
            <Descriptions.Item label="电话">{selectedReviewer.phone || '-'}</Descriptions.Item>
            <Descriptions.Item label="机构">{selectedReviewer.institution || '-'}</Descriptions.Item>
            <Descriptions.Item label="研究领域">{selectedReviewer.researchField || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={selectedReviewer.active ? 'green' : 'red'}>
                {selectedReviewer.active ? '启用' : '禁用'}
              </Tag>
            </Descriptions.Item>
            {selectedReviewer.conflictOfInterestInstitutions?.length > 0 && (
              <Descriptions.Item label="利益冲突机构">
                {selectedReviewer.conflictOfInterestInstitutions.join(', ')}
              </Descriptions.Item>
            )}
            <Descriptions.Item label="创建时间">{selectedReviewer.createdAt}</Descriptions.Item>
            <Descriptions.Item label="审稿记录数">
              {selectedReviewer.reviewIds?.length || 0}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
}

export default Reviewers;
