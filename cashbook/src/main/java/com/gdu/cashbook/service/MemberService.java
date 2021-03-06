package com.gdu.cashbook.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gdu.cashbook.mapper.CashMapper;
import com.gdu.cashbook.mapper.MemberMapper;
import com.gdu.cashbook.mapper.MemberidMapper;
import com.gdu.cashbook.vo.Cash;
import com.gdu.cashbook.vo.Cashbook;
import com.gdu.cashbook.vo.LoginMember;
import com.gdu.cashbook.vo.Member;
import com.gdu.cashbook.vo.MemberForm;
import com.gdu.cashbook.vo.Memberid;

@Service
@Transactional
public class MemberService {
	@Autowired private MemberMapper memberMapper;
	@Autowired private MemberidMapper memberidMapper;
	@Autowired private JavaMailSender javaMailSender; //bean생성 -> @Component
	
	@Autowired private CashMapper cashMapper;
	
//	@Value("C:\\javaweb\\SPRING_WORK\\maven.1590233389670\\cashbook\\src\\main\\resources\\static\\upload")
//	private String path;
	
	@Value("D:\\git-cashbook\\maven.1590631938256\\cashbook\\src\\main\\resources\\static\\upload")
	private String path;
	
	public int removeMemberOut (String memberId) {
		int deleteCashResult = cashMapper.deleteCashBeforeOut(memberId);
		if(deleteCashResult == 1) {
			System.out.println("수입/지출 삭제 성공");
		} else {
			System.out.println("수입/지출 삭제 실패");
		}
		int deleteCashbookResult = cashMapper.deleteCashbookBeforeOut(memberId);
		if(deleteCashbookResult == 1) {
			System.out.println("가계부 삭제 성공");
		} else {
			System.out.println("가계부 삭제 실패");
		}
		int deleteMember = memberMapper.deleteMemberOut(memberId);
		Memberid memberid = new Memberid();
		memberid.setMemberId(memberId);
		memberidMapper.insertMemberid(memberid);
		return deleteMember;
	}
	
	public Map<String, Object> getAdminMemberInfoList(int currentPage) {
		int rowPerPage = 7;
		int beginRow = (currentPage - 1) * rowPerPage;
		int lastPage = getLastPage(rowPerPage);
		List<Member> list = memberMapper.selectAdminMemberInfoList(beginRow, rowPerPage);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("lastPage", lastPage);
		map.put("list", list);
		
		return map;
	}
	public int getLastPage(int rowPerPage) {
		int totalRow = memberMapper.selectAdminMemberInfoTotalRow();
		int lastPage = totalRow / rowPerPage;
		if(totalRow % rowPerPage != 0) {
			lastPage += 1;
		}
		return lastPage;
	}
	
	public int getMemberPw(Member member) { // id & pw만 들어있다.
		// pw도 추가해주자(UUID 라이브러리 사용)
		UUID uuid = UUID.randomUUID();
	
		String memberPw = uuid.toString().substring(0, 8);
		member.setMemberPw(memberPw);
		int row = memberMapper.updateMemberPw(member);
		
		if(row == 1) {
			// 성공하면 변경된 pw를 이메일로 보내준다.(메일객체) new JavaMailSender();
			System.out.println(memberPw + "<--- update memberPw");
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setTo(member.getMemberEmail()); // 받는 주소
			simpleMailMessage.setFrom("imdoer0702@gmail.com"); // 보내는 곳
			simpleMailMessage.setSubject("cashbook 사이트 비밀번호 찾기 결과입니다."); // 메일 제목
			simpleMailMessage.setText("변경된 비밀번호 : " + memberPw + "  로그인 후에 꼭 비밀번호를 변경하세요."); // 메일 내용
			javaMailSender.send(simpleMailMessage);
		}
		return row;
	}
	
	public String getMemberIdByMember(Member member) {
		return memberMapper.selectMemberIdBymember(member);
	}
	public void removeMember(LoginMember loginMember) {
		// @Transactional 이 여기에 있어도 괜찮다.
		// 1. 회원 이미지 삭제
		// 1-1 파일 이름 select member_pic from member
		String memberPic = memberMapper.selectMemberPic(loginMember.getMemberId());
		// 1-2 파일 삭제
		File file = new File(path + memberPic);
		if(file.exists()) {
			file.delete();
		}
		// 2.
		cashMapper.deleteCashBeforeSignout(loginMember);
		cashMapper.deleteCashbookBeforeSignout(loginMember);
		memberMapper.deleteMember(loginMember);
		
		Memberid memberid = new Memberid();
		memberid.setMemberId(loginMember.getMemberId());
		memberidMapper.insertMemberid(memberid);
		
	}
	public String modifyPwCk(String memberPw) {
		return memberMapper.updatePwCk(memberPw);
	}
	public String getModifyMemberPic(LoginMember loginMember) {
		return memberMapper.selectMemberPic(loginMember.getMemberId());
	}
	
	public Member getModifyMemberOne(LoginMember loginMember) {
		return memberMapper.updateMemberOne(loginMember);
	}
	
	public Member getMemberOne(LoginMember loginMember) {
		return memberMapper.selectMemberOne(loginMember);
	}
	
	public String checkmemberId(String memberIdCk) {
		return memberMapper.selectCheckMemberId(memberIdCk);
	}
	
	public LoginMember login(LoginMember loginMember) {
		return memberMapper.selectLoginMember(loginMember);
	}
	public int modifyMemberInfo(MemberForm memberForm, LoginMember loginMember) {
	      // 기존에 있던 프로필 사진을 삭제(만약 사진 수정을 안 한다면?)
	      String memberPic = memberMapper.selectMemberPic(loginMember.getMemberId());
	      System.out.println(memberForm.getMemberPic().toString() + "<-- memberPic");
	      // 수정한 사진을 업로드
	      MultipartFile mf = memberForm.getMemberPic();
	      String originName = mf.getOriginalFilename();
	      String newMemberPic = null;
	      if(originName.equals("")) {
	         newMemberPic = memberPic;
	      } else {
	         File originFile = new File(path+memberPic);
	         // 새로운 파일 입력되면 그 전 파일 삭제
	         if(originFile.exists() && !memberPic.equals("default.jpg")) {
	            originFile.delete();
	         }
	         int lastDot = originName.lastIndexOf(".");
	         String extension = originName.substring(lastDot);
	         System.out.println(extension);
	         newMemberPic = memberForm.getMemberId()+extension;
	         System.out.println(newMemberPic);
	      }
	      System.out.println(newMemberPic + " <--memberPic modifyMemberInfo");
	   
	      Member member = new Member();
	      member.setMemberId(memberForm.getMemberId());
	      member.setMemberName(memberForm.getMemberName());
	      member.setMemberAddr(memberForm.getMemberAddr());
	      member.setMemberPhone(memberForm.getMemberPhone());
	      member.setMemberEmail(memberForm.getMemberEmail());
	      member.setMemberPic(newMemberPic);
	      
	      System.out.println(member.getMemberName());
	      System.out.println(member.getMemberAddr());
	      System.out.println(member.getMemberPhone());
	      System.out.println(member.getMemberEmail());
	      System.out.println(member.getMemberPic());
	      
	      int row =  memberMapper.updateMemberInfo(member);
	      
	   
	      File file = new File(path +"\\"+ newMemberPic);
	      try {
	         mf.transferTo(file);
	      } catch (IllegalStateException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	         throw new RuntimeException();
	      }
	      
	      
	      return row;
	   }
	
	public int addMember(MemberForm memberForm) {
		// MemberForm -> member
		// member -> 디스크에 물리적으로 저장
		MultipartFile mf = memberForm.getMemberPic();
		
		// 확장자가 필요함 xxx.jpg 
		String originName = mf.getOriginalFilename();
		//mf.getContentType().equals("image/PNG"); // 분기해서 특정 파일 확장자를 지정할 수도 있다.
		System.out.println(originName);
			
		// 마지막 점의 위치
		int lastDot = originName.lastIndexOf(".");
		String extension = originName.substring(lastDot);
		
		// memberId + 확장자명
		String memberPic = memberForm.getMemberId() + extension;
		
		// 1. db에 저장
		Member member = new Member();
		member.setMemberId(memberForm.getMemberId());
		member.setMemberPw(memberForm.getMemberPw());
		member.setMemberName(memberForm.getMemberName());
		member.setMemberPhone(memberForm.getMemberPhone());
		member.setMemberAddr(memberForm.getMemberAddr());
		member.setMemberEmail(memberForm.getMemberEmail());
		member.setMemberDate(member.getMemberDate());
		member.setMemberPic(memberPic);
		System.out.println(member + "<-- MemberService addMember member");
		int row = memberMapper.insertMember(member);
		
		// 2. 파일저장(static/upload)
		
		File file = new File(path + "\\" + memberPic);
		try {
			mf.transferTo(file);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace(); // 아래 코드가 없으면 여기서 끝나버린다.
			throw new RuntimeException(); // 그래서 다시 예외를 발생시킨다. -> @Transactional이 rollback 시킨다.
			// Exception
			// 1. 문법적으로 반드시 예외처리를 해야만 하는 경우
			// 2. 예외처리를 하지 않아도 되는 경우 ex) RuntimeException
		}
		// 3. service 보내기
		
		return row;
	}
}
