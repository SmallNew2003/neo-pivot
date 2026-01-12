package com.jelvin.neopivot.common.events;

/**
 * 文档上传完成事件。
 *
 * <p>当文档元数据成功创建（或确认）且事务提交后发布，用于触发异步索引等流程。
 *
 * @param documentId 文档 ID
 * @author Jelvin
 */
public record DocumentUploadedEvent(Long documentId) implements DomainEvent {}

