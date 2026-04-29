import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Switch, message, Popconfirm, Tag, Descriptions } from 'antd';
import { PlusOutlined, EditOutlined, EyeOutlined, BlockOutlined, UnlockOutlined } from '@ant-design/icons';
import { authorApi } from '../api';

const { TextArea } = Input;

function Authors() {
  const [authors, setAuthors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedAuthor, setSelectedAuthor] = useState(null);
  const [form] = Form.useForm();
  const [isEdit, setIsEdit] = useState(false);

  useEffect(() => {
    fetchAuthors();
  }, []);

  const fetchAuthors = async () => {
    setLoading(true);
    try {
      const res = await authorApi.getAll();
      setAuthors(res.data || []);
    } catch (error) {
      message.error('加载作者列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setIsEdit(false);
    form.resetFields();
    setSelectedAuthor(null);
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setIsEdit(true);
    setSelectedAuthor(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDetail = (record) => {
    setSelectedAuthor(record);
    setDetailVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (isEdit && selectedAuthor) {
        await authorApi.update(selectedAuthor.id, values);
        message.success('更新成功');
      } else {
        await authorApi.create(values);
        message.success('创建成功');
      }
      
      setModalVisible(false);
      fetchAuthors();
    } catch (error) {
      message.error(isEdit ? '更新失败' : '创建失败');
      console.error(error);
    }
  };

  const handleBlacklist = async (record, blacklisted, reason) => {
    try {
      await authorApi.setBlacklist(record.id, {
        blacklisted,
        reason: blacklisted ? reason : null,
      });
      message.success(blacklisted ? '已加入黑名单' : '已移除黑名单');
      fetchAuthors();
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
      title: '状态',
      dataIndex: 'blacklisted',
      key: 'blacklisted',
      render: (blacklisted, record) => (
        <Tag color={blacklisted ? 'red' : 'green'}>
          {blacklisted ? '黑名单' : '正常'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
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
          {record.blacklisted ? (
            <Button
              type="link"
              icon={<UnlockOutlined />}
              onClick={() => handleBlacklist(record, false)}
            >
              移除黑名单
            </Button>
          ) : (
            <Popconfirm
              title="确定要将该作者加入黑名单吗？"
              onConfirm={() => {
                Modal.confirm({
                  title: '请输入黑名单原因',
                  content: (
                    <Input.TextArea
                      id="blacklistReason"
                      placeholder="请输入原因..."
                      rows={3}
                    />
                  ),
                  onOk: () => {
                    const reason = document.getElementById('blacklistReason')?.value;
                    handleBlacklist(record, true, reason || '未提供原因');
                  },
                });
              }}
            >
              <Button type="link" danger icon={<BlockOutlined />}>
                加入黑名单
              </Button>
            </Popconfirm>
          )}
        </>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加作者
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={authors}
        rowKey="id"
        loading={loading}
        locale={{ emptyText: '暂无作者数据' }}
      />

      <Modal
        title={isEdit ? '编辑作者' : '添加作者'}
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
          <Form.Item name="address" label="地址">
            <TextArea placeholder="请输入地址" rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="作者详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {selectedAuthor && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="ID">{selectedAuthor.id}</Descriptions.Item>
            <Descriptions.Item label="姓名">{selectedAuthor.name}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{selectedAuthor.email}</Descriptions.Item>
            <Descriptions.Item label="电话">{selectedAuthor.phone || '-'}</Descriptions.Item>
            <Descriptions.Item label="机构">{selectedAuthor.institution || '-'}</Descriptions.Item>
            <Descriptions.Item label="地址">{selectedAuthor.address || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={selectedAuthor.blacklisted ? 'red' : 'green'}>
                {selectedAuthor.blacklisted ? '黑名单' : '正常'}
              </Tag>
            </Descriptions.Item>
            {selectedAuthor.blacklistReason && (
              <Descriptions.Item label="黑名单原因">
                {selectedAuthor.blacklistReason}
              </Descriptions.Item>
            )}
            <Descriptions.Item label="创建时间">{selectedAuthor.createdAt}</Descriptions.Item>
            <Descriptions.Item label="投稿数量">
              {selectedAuthor.submissionIds?.length || 0}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
}

export default Authors;
