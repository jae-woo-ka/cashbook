package com.gdu.cashbook.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.gdu.cashbook.vo.Cash;
import com.gdu.cashbook.vo.Cashbook;
import com.gdu.cashbook.vo.Category;
import com.gdu.cashbook.vo.dayAndMonthAndYearAndPrice;

@Mapper
public interface CashMapper {
	public Integer selectTotalMonthSum(Map<String, Object> map);
	public List<dayAndMonthAndYearAndPrice> selectDaySum(Map<String, Object> map);
	public int insertCashBook(Cashbook cashbook);
	public int cashbookTotalRow(String memberId);
	public List<Cashbook> selectCashbookList(Map<String, Object> map);
	public Integer selectTotalDateSum(Map<String, Object> map);
	public int updateCash(Cash cash);
	public Cash selectCashOne(int cashNo);
	public List<Category> selectCategoryList();
	public List<dayAndMonthAndYearAndPrice> selectDayAndPrice(Map<String, Object> day);
	public int insertCash(Cash cash);
	public void deleteCash(int cashNo, int cashbookNo);
	public Integer selectSumCashByDate(Cash cash);
	public List<Cash> selectCashListByDate(Cash cash);
}
