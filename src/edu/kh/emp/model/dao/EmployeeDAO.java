package edu.kh.emp.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.kh.emp.model.vo.Employee;

// DAO(Data Access Object, 데이터 접근 객체)
// -> 데이터베이스에 접근(연결)하는 객체
// --> JDBC 코드 작성
//Run - View - (Service) - DAO - VO 순으로 탭을 열어두는 것이 좋다

public class EmployeeDAO {
	
	
	// JDBC 객체 참조 변수 필드 선언 (class 내부에 공통 사용)
	// heap 메모리에 생성되는 객체들은 값이 비어있을 수 없기에 초기화만 시켜놓으면 됨
	// JVM이 알아서 값을 초기화 시켜놓음
		
	private Connection conn; // 필드(Heap, 변수가 비어있을 수 없다)
	private Statement stmt; // 초기화를 안해도 JVM이 지정한 기본 값으로 초기화한다.
	private ResultSet rs = null; // -> 참조형의 초기값은 null
								 // 별도 초기화 안해도 된다!
	
	private PreparedStatement pstmt;
	// Statement의 자식으로 향상된 기능을 제공
	// -> ? 기호 (placeholder / 위치홀더)를 이용해서
	// SQL에 작성되어지는 리터럴을 동적으로 제어하는 용도
	
	// SQL ? 기호에 추가되는 값은
	// 숫자인 경우 '' 없이 대입
	// 문자열인 경우 ''가 자동으로 추가되어 대입
	
	
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@localhost:1521:XE";
	private String user = "kh";
	private String pw = "kh1234";
	
	
//	public void method() {
//		Connection conn2; // 지역변수(Stack영역, 변수 초기화 안하면 비어있을 수 있다.)
//	}
	
	
	// 메서드 주석 다는 방법 alt + shift + j
	
	/** 전체 사원 정보 조회 DAO
	 * @ return empList
	 */
	public List<Employee> selectAll() {
		
		// 1. 결과 저장용 변수 선언
		
		List<Employee>empList = new ArrayList<>();
		
		try {
			// 2. JDBC 참조 변수에 객체 대입
			// -> conn, stmt, rs에 객체 대입
			
			Class.forName(driver); // oracle jdbc 드라이버 객체 메모리 로드
			
			
			conn = DriverManager.getConnection(url, user, pw);
			
			String sql = "SELECT EMP_ID , EMP_NAME , EMP_NO , EMAIL , PHONE , \r\n"
					+ "NVL(DEPT_TITLE, '부서없음') AS DEPT_TITLE, JOB_NAME, SALARY \r\n"
					+ "FROM EMPLOYEE\r\n"
					+ "LEFT JOIN DEPARTMENT ON(DEPT_CODE = DEPT_ID)\r\n"
					+ "JOIN JOB USING(JOB_CODE)";
			
			
			// 오라클 jdbc 드라이버 객체를 이용하여 DB 접속 방법 생성
			
			
			// Statement 객체 생성
			stmt = conn.createStatement();
			
			
			// SQL을 수행 후 결과(ResultSet) 반환 받음
			rs = stmt.executeQuery(sql);
			
			// 3. 조회 결과를 얻어와 한 행씩 접근하여
			// Employee 객체 생성 후, 컬럼 값 옮겨 담기
			// List에 추가
			
			while(rs.next()){
				
				int empId = rs.getInt("EMP_ID");
				// EMP_ID 컬럼은 문자열 컬럼이지만, 
				// 저장된 값들이 숫자형태임
				// -> DB에서 자동으로 형변환 진행해서 얻어옴
				String empName = rs.getString("EMP_NAME");
				String empNo = rs.getString("EMP_NO");
				String email = rs.getString("EMAIL");
				String phone = rs.getString("PHONE");
				String deptTitle = rs.getString("DEPT_TITLE");
				String jobName = rs.getString("JOB_NAME");
				int salary = rs.getInt("SALARY");
				
				Employee emp = new Employee(empId, empName, empNo, email, phone, deptTitle, jobName, salary);				
				empList.add(emp); // List 담기
			}// while 문 종료
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			// 4. JDBC 객체 자원 반환
			try {
				if (rs != null) rs.close();
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		// 5. 결과 반환
		return empList;
	}
	
	/** 주민번호가 일치하는 사원 정보 조회 DAO
	 * @param empNo
	 * @return emp
	 */
	public Employee selectEmpNo(String empNo) {
		
		// 결과 저장용 변수 선언
		
		Employee emp = null;
		
		try {
			
			Class.forName(driver);
			
			conn = DriverManager.getConnection(url, user, pw);
			
			// SQL 작성
			String sql = "SELECT EMP_ID , EMP_NAME , EMP_NO , EMAIL , PHONE , \r\n"
					+ "NVL(DEPT_TITLE, '부서없음') DEPT_TITLE, JOB_NAME, SALARY \r\n"
					+ "FROM EMPLOYEE\r\n"
					+ "LEFT JOIN DEPARTMENT ON(DEPT_CODE = DEPT_ID)\r\n"
					+ "JOIN JOB USING(JOB_CODE)\r\n"
					+ "WHERE EMP_NO = ?";
									// ? -> placeholder 
			// Statement는 객체 사용 시 순서
			// SQL작성 -> Statement 생성 -> SQL 수행 후 결과 반환
			
			// PreparedStatement 객체 사용 시 순서
			// SQL 작성
			// -> PreparedStatement 객체 생성( ? 가 포함된 SQL을 매개변수로 사용)
			// -> ? 에 알맞은 값 대입
			// -> SQL 수행 후 결과 반환
			
			// PreparedStatement 객체 생성
			pstmt = conn.prepareStatement(sql);
			
			// ? 에 알맞은 값 대입
			pstmt.setString(1, empNo);
			
			// SQL 수행 후 결과 반환
			rs = pstmt.executeQuery();
			// PreparedStatement는 
			// 객체 생성 시, 이미 SQL이 담겨져 있는 상태이므로
			// SQL 수행(excuteQuery()) 시 매개변수로 전달할 필요가 없다.
			
			// pstmt.setString(1, empNo);
			// -> ?에 작성되어있던 값이 모두 사라져 수행 시 오류 발생
			
			
			if(rs.next()) {
				int empId = rs.getInt("EMP_ID");
				String empName = rs.getString("EMP_NAME");
				// String empNo = rs.getString("EMP_NO"); -> 파라미터와 같은 값이므로 불필요하다.
				String email = rs.getString("EMAIL");
				String phone = rs.getString("PHONE");
				String deptTitle = rs.getString("DEPT_TITLE");
				String jobName = rs.getString("JOB_NAME");
				int salary = rs.getInt("SALARY");
				
				emp = new Employee(empId, empName, empNo, email, phone, deptTitle, jobName, salary);		
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			
			try {
				if( rs != null) rs.close();
				if( pstmt != null) pstmt.close();
				if( conn != null) conn.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}	
		}
		return emp;
	}	
	
	
	
	/**
	 * @param emp
	 * @return result(INSERT 성공한 행의 개수 반환)
	 */
	public int insertEmployee(Employee emp) {
		// 결과 저장용 변수 선언
		int result = 0;
		
		try { 
			// 커넥션 생성
			
			Class.forName(driver);
			conn = DriverManager.getConnection(url , user , pw);
			
			// ** DML 수행할 예정 **
			// Transaction에 DML 구문이 임시 저장
			// --> 정상적인 DML인지를 판별해서 개발자가 직접 commit , rollback을 수행
			
			// Connection 객체 생성 시
			// AutoCommit이 활성화 되어 있는 상태이기 때문에
			// 이를 해제하는 코드를 추가!
			
			conn.setAutoCommit(false); // AutoCommit 비활성화
			
			
			// AutoCommit 비활성화를 해도
			// conn.close(); 구문이 수행되면 자동으로 Commit이 수행된다.
			// --> close() 수행 전에 트랜잭션 제어 코드를 작성해야 한다!
			
			// SQL 작성
			String sql = 
					"INSERT INTO EMPLOYEE VALUES"
					+ " (?,?,?,?,?,?,?,?,?,?,?, SYSDATE, NULL, DEFAULT)";
			// 퇴사 여부 컬럼의 DEFAULT == 'N'
			
			
			// PreparedStatement 객체 생성(매개변수에 SQL 추가)
			pstmt = conn.prepareStatement(sql);
			
			// ? (placeholder)에 알맞은 값 대입
			
			pstmt.setInt(1, emp.getEmpId());
			pstmt.setString(2, emp.getEmpName());
			pstmt.setString(3, emp.getEmpNo());
			pstmt.setString(4, emp.getEmail());
			pstmt.setString(5, emp.getPhone());
			pstmt.setString(6, emp.getDeptCode());
			pstmt.setString(7, emp.getJobCode());
			pstmt.setString(8, emp.getSalLevel());
			pstmt.setInt(9, emp.getSalary());
			pstmt.setDouble(10, emp.getBonus());
			pstmt.setInt(11, emp.getManagerId());
			
			
			// SQL 수행 후 결과 반환 받기
			result = pstmt.executeUpdate();
			
			// executeQuery() : SELECT 수행 후 , ResultSet 반환
			// executeUpdate() : DML(INSERT, UPDATE, DELETE) 수행 후 결과 행 개수 반환
			
			// ** 트랜잭션 제어 처리 **
			// -> DML 성공 여부에 따라서 commit, rollback 제어
			
			if(result > 0 ) conn.commit(); // DML 성공 시, commit
			else			conn.rollback(); // DML 실패 시, rollback
			
		}catch(Exception e) {
			e.printStackTrace();
			
		}finally {
			try {
				if( pstmt != null) pstmt.close();
				if( conn != null) conn.close();
				
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}	
	
	
	
	/** 사번이 일치하는 사원 정보 수정 DAO
	 * @param emp
	 * @return
	 */
	public int updateEmployee(Employee emp) {
		int result = 0;
		
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, pw);
			conn.setAutoCommit(false); // AutoCommit 비활성화
			
			String sql = "UPDATE EMPLOYEE SET "
					+ " EMAIL = ?, PHONE = ?, SALARY = ? "
					+ " WHERE EMP_ID = ?";
			// PreparedStatement 생성
			pstmt = conn.prepareStatement(sql);
			
			
			// ? 에 알맞은 값 세팅
			pstmt.setString(1, emp.getEmail());
			pstmt.setString(2, emp.getPhone());
			pstmt.setInt(3, emp.getSalary());
			pstmt.setInt(4, emp.getEmpId());
			
			result = pstmt.executeUpdate(); // 반영된 행의 개수 반환
		
			// 트랜잭션 제어 처리
			if(result == 0) conn.rollback();
			else 			conn.commit();
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if( pstmt != null) pstmt.close();
				if( conn != null) conn.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	
	/** 사번이 일치하는 사원 정보 삭제 DAO
	 * @param empId
	 * @return
	 */
	public int deleteEmployee(int empId) {
		int result = 0; // 결과 저장용 변수
		
		
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, pw);
			conn.setAutoCommit(false);
			
			String sql = "DELETE FROM EMPLOYEE WHERE EMP_ID = ?";
			
			
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1,empId);
			
			result = pstmt.executeUpdate();
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	
	
	
	public List<Employee> selectDeptEmp(String departmentTitle) {
		
		List<Employee>empList = new ArrayList<>();
		Employee emp = new Employee();
		
		
		try {
			
			
			
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, pw);
			
			String sql = "SELECT EMP_ID , EMP_NAME , EMP_NO , EMAIL , PHONE , \r\n"
					+ "NVL(DEPT_TITLE, '부서없음') DEPT_TITLE, JOB_NAME, SALARY \r\n"
					+ "FROM EMPLOYEE\r\n"
					+ "LEFT JOIN DEPARTMENT ON(DEPT_CODE = DEPT_ID)\r\n"
					+ "JOIN JOB USING(JOB_CODE)\r\n"
					+ "WHERE DEPT_TITLE IN ?";

			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, departmentTitle);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				int empId = rs.getInt("EMP_ID");
				String empName = rs.getString("EMP_NAME");
				String empNo = rs.getString("EMAIL");
				String email = rs.getString("EMAIL");
				String phone = rs.getString("PHONE");
				String jobName = rs.getString("JOB_NAME");
				int salary = rs.getInt("SALARY");
				emp = new Employee(empId, empName, empNo, email, phone, departmentTitle, jobName, salary);
				
				empList.add(emp);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
			}catch(Exception e) {
				
				e.printStackTrace();
			}
		}
		return empList;
	}
	
	
	
	public List<Employee> selectSalary (int salary) {
		
		Employee emp = new Employee();
		
		List<Employee>empList = new ArrayList<>();
		
		
		
		
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url , user , pw);
			
			String sql = "SELECT EMP_ID , EMP_NAME , EMP_NO , EMAIL , PHONE , \r\n"
					+ "NVL(DEPT_TITLE, '부서없음') DEPT_TITLE, JOB_NAME, SALARY \r\n"
					+ "FROM EMPLOYEE\r\n"
					+ "LEFT JOIN DEPARTMENT ON(DEPT_CODE = DEPT_ID)\r\n"
					+ "JOIN JOB USING(JOB_CODE)\r\n"
					+ "WHERE SALARY > ?";
			
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, salary);
			
			
			rs = pstmt.executeQuery();
			
			
			while(rs.next()) {
				int empId = rs.getInt("EMP_ID");
				String empName = rs.getString("EMP_NAME");
				String empNo = rs.getString("EMP_NO");
				String email = rs.getString("EMAIL");
				String phone = rs.getString("PHONE");
				String deptTitle = rs.getString("DEPT_TITLE");
				String jobName = rs.getString("JOB_NAME");
				int salary1 = rs.getInt("SALARY");
				
				emp = new Employee(empId, empName, empNo, email, phone, deptTitle, jobName, salary1);
				empList.add(emp);
				
			}
			
		}catch(Exception e) {
				e.printStackTrace();
		}finally {
			try {
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return empList;
	}
	
	
	
	
	
	
	public Employee selectEmpId(int empId){
		
		Employee emp = null;
		
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, pw);
			
			String sql = "SELECT EMP_ID, EMP_NAME, EMP_NO , EMAIL,PHONE ,NVL(DEPT_TITLE, '부서없음') AS DEPT_TITLE, JOB_NAME, SALARY \r\n"
					+ "FROM EMPLOYEE \r\n"
					+ "LEFT JOIN DEPARTMENT ON(DEPT_ID = DEPT_CODE)\r\n"
					+ "JOIN JOB USING(JOB_CODE)\r\n"
					+ "WHERE EMP_ID = ?";
			
			pstmt = conn.prepareStatement(sql);
			
			
			
			pstmt.setInt(1, empId);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				
				String empName = rs.getString("EMP_NAME");
				String empNo = rs.getString("EMP_NO");
				String email = rs.getString("EMAIL");
				String phone = rs.getString("PHONE");
				String departmentTitle = rs.getString("DEPT_TITLE");
				String jobName = rs.getString("JOB_NAME");
				int salary = rs.getInt("SALARY");
				
				emp = new Employee(empId, empName, empNo, email, phone, departmentTitle, jobName, salary);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return emp;
	}
}