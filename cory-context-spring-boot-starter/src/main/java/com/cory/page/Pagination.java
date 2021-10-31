package com.cory.page;

import java.util.List;

/**
 * 列表分页。包含list属性。
 */
public class Pagination<T> implements java.io.Serializable {

	private static final long serialVersionUID = 1870874231783497584L;

	public static final int DEFAULT_PAGE_SIZE = 20;

	private int totalCount = 0;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private int pageNo = 1;

	private List<T> list;

	public Pagination() {
	}

	/**
	 * 构造器
	 *
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页几条数据
	 * @param totalCount
	 *            总共几条数据
	 */
	public Pagination(int pageNo, int pageSize, int totalCount) {
		this(pageNo, pageSize, totalCount, null);
	}

	/**
	 * 构造器
	 *
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页几条数据
	 * @param totalCount
	 *            总共几条数据
	 * @param list
	 *            分页内容
	 */
	public Pagination(int pageNo, int pageSize, int totalCount, List<T> list) {
		setTotalCount(totalCount);
		setPageSize(pageSize);
		setPageNo(pageNo);
		adjustPageNo();

		this.list = list;
	}

	/**
	 * 第一条数据位置
	 *
	 * @return
	 */
	public int getFirstResult() {
		return (pageNo - 1) * pageSize;
	}

	/**
	 * 获得分页内容
	 *
	 * @return
	 */
	public List<T> getList() {
		return list;
	}

	/**
	 * 设置分页内容
	 *
	 * @param list
	 */
	public void setList(List<T> list) {
		this.list = list;
	}

	/**
	 * 检查页码 checkPageNo
	 *
	 * @param pageNo
	 * @return if pageNo==null or pageNo<1 then return 1 else return pageNo
	 */
	public static int cpn(Integer pageNo) {
		return (pageNo == null || pageNo < 1) ? 1 : pageNo;
	}

	/**
	 * 调整页码，使不超过最大页数
	 */
	public void adjustPageNo() {
		if (pageNo == 1) {
			return;
		}
		int tp = getTotalPage();
		if (pageNo > tp) {
			pageNo = tp;
		}
	}

	/**
	 * 获得页码
	 */
	public int getPageNo() {
		return pageNo;
	}

	/**
	 * 每页几条数据
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * 总共几条数据
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * 总共几页
	 */
	public int getTotalPage() {
		int totalPage = totalCount / pageSize;
		if (totalPage == 0 || totalCount % pageSize != 0) {
			totalPage++;
		}
		return totalPage;
	}

	/**
	 * 是否第一页
	 */
	public boolean isFirstPage() {
		return pageNo <= 1;
	}

	/**
	 * 是否最后一页
	 */
	public boolean isLastPage() {
		return pageNo >= getTotalPage();
	}

	/**
	 * 下一页页码
	 */
	public int getNextPage() {
		if (isLastPage()) {
			return pageNo;
		} else {
			return pageNo + 1;
		}
	}

	/**
	 * 上一页页码
	 */
	public int getPrevPage() {
		if (isFirstPage()) {
			return pageNo;
		} else {
			return pageNo - 1;
		}
	}


	/**
	 * if totalCount<0 then totalCount=0
	 *
	 * @param totalCount
	 */
	public void setTotalCount(int totalCount) {
		if (totalCount < 0) {
			this.totalCount = 0;
		} else {
			this.totalCount = totalCount;
		}
	}

	/**
	 * if pageSize< 1 then pageSize=DEF_COUNT
	 *
	 * @param pageSize
	 */
	public void setPageSize(int pageSize) {
		if (pageSize < 1) {
			this.pageSize = DEFAULT_PAGE_SIZE;
		} else {
			this.pageSize = pageSize;
		}
	}

	/**
	 * if pageNo < 1 then pageNo=1
	 *
	 * @param pageNo
	 */
	public void setPageNo(int pageNo) {
		if (pageNo < 1) {
			this.pageNo = 1;
		} else {
			this.pageNo = pageNo;
		}
	}
}
