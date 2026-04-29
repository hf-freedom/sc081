import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, message, Tag, Descriptions, Input, Space, Select } from 'antd';
import { EyeOutlined, SearchOutlined, DownloadOutlined } from '@ant-design/icons';
import { publicationApi, submissionApi, authorApi } from '../api';

const { Option } = Select;

function Publications() {
  const [publications, setPublications] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedPublication, setSelectedPublication] = useState(null);
  const [filterYear, setFilterYear] = useState(null);

  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 5 }, (_, i) => currentYear - i);

  useEffect(() => {
    fetchData();
  }, [filterYear]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [publicationsRes, submissionsRes, authorsRes] = await Promise.all([
        filterYear ? publicationApi.getByYear(filterYear) : publicationApi.getAll(),
        submissionApi.getAll(),
        authorApi.getAll(),
      ]);
      setPublications(publicationsRes.data || []);
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
    setSelectedPublication(record);
    setDetailVisible(true);
  };

  const columns = [
    {
      title: '出版记录ID',
      dataIndex: 'id',
      key: 'id',
      width: 100,
    },
    {
      title: 'DOI',
      dataIndex: 'doi',
      key: 'doi',
      ellipsis: true,
      render: (doi) => (
        <a href={`https://doi.org/${doi}`} target="_blank" rel="noopener noreferrer">
          {doi}
        </a>
      ),
    },
    {
      title: '投稿ID',
      dataIndex: 'submissionId',
      key: 'submissionId',
      width: 80,
    },
    {
      title: '标题',
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
      title: '卷期',
      key: 'volume',
      render: (_, record) => (
        <span>第{record.volume}卷 第{record.issue}期</span>
      ),
    },
    {
      title: '页码',
      key: 'pages',
      render: (_, record) => (
        <span>{record.startPage} - {record.endPage}</span>
      ),
    },
    {
      title: '出版日期',
      dataIndex: 'publicationDate',
      key: 'publicationDate',
      width: 120,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
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
          {record.pdfUrl && (
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => window.open(record.pdfUrl, '_blank')}
            >
              PDF
            </Button>
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
          placeholder="选择年份"
          allowClear
          value={filterYear}
          onChange={setFilterYear}
        >
          {years.map(y => (
            <Option key={y} value={y}>{y}年</Option>
          ))}
        </Select>
      </div>

      <Table
        columns={columns}
        dataSource={publications}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: '暂无出版记录' }}
      />

      <Modal
        title="出版详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {selectedPublication && (
          <div>
            <Descriptions bordered column={2} title="出版信息">
              <Descriptions.Item label="出版记录ID">{selectedPublication.id}</Descriptions.Item>
              <Descriptions.Item label="投稿ID">{selectedPublication.submissionId}</Descriptions.Item>
              <Descriptions.Item label="DOI" span={2}>
                <a href={`https://doi.org/${selectedPublication.doi}`} target="_blank" rel="noopener noreferrer">
                  {selectedPublication.doi}
                </a>
              </Descriptions.Item>
              <Descriptions.Item label="卷">
                第{selectedPublication.volume}卷
              </Descriptions.Item>
              <Descriptions.Item label="期">
                第{selectedPublication.issue}期
              </Descriptions.Item>
              <Descriptions.Item label="起始页码">{selectedPublication.startPage}</Descriptions.Item>
              <Descriptions.Item label="结束页码">{selectedPublication.endPage}</Descriptions.Item>
              <Descriptions.Item label="出版日期">{selectedPublication.publicationDate}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{selectedPublication.createdAt}</Descriptions.Item>
            </Descriptions>

            <Descriptions bordered column={1} title="访问链接" style={{ marginTop: 16 }}>
              <Descriptions.Item label="PDF">
                {selectedPublication.pdfUrl ? (
                  <a href={selectedPublication.pdfUrl} target="_blank" rel="noopener noreferrer">
                    {selectedPublication.pdfUrl}
                  </a>
                ) : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="XML">
                {selectedPublication.xmlUrl ? (
                  <a href={selectedPublication.xmlUrl} target="_blank" rel="noopener noreferrer">
                    {selectedPublication.xmlUrl}
                  </a>
                ) : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="HTML">
                {selectedPublication.htmlUrl ? (
                  <a href={selectedPublication.htmlUrl} target="_blank" rel="noopener noreferrer">
                    {selectedPublication.htmlUrl}
                  </a>
                ) : '-'}
              </Descriptions.Item>
            </Descriptions>

            {(() => {
              const submission = submissions.find(s => s.id === selectedPublication.submissionId);
              const author = authors.find(a => a.id === submission?.authorId);
              if (submission) {
                return (
                  <Descriptions bordered column={2} title="投稿信息" style={{ marginTop: 16 }}>
                    <Descriptions.Item label="标题" span={2}>
                      {submission.title}
                    </Descriptions.Item>
                    <Descriptions.Item label="作者">{author?.name || '-'}</Descriptions.Item>
                    <Descriptions.Item label="页数">{submission.pageCount}</Descriptions.Item>
                    <Descriptions.Item label="摘要" span={2}>
                      {submission.abstracts}
                    </Descriptions.Item>
                    <Descriptions.Item label="关键词">{submission.keywords || '-'}</Descriptions.Item>
                    <Descriptions.Item label="提交时间">{submission.submittedAt}</Descriptions.Item>
                  </Descriptions>
                );
              }
              return null;
            })()}
          </div>
        )}
      </Modal>
    </div>
  );
}

export default Publications;
