"use client";

import { useState, useEffect } from "react";
import { usePathname } from "next/navigation";
import { Navbar, Container, Nav, NavDropdown, Row, Col, Form, Button } from "react-bootstrap";

export default function MainNavbar() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const pathname = usePathname(); 
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    setIsLoggedIn(!!token);
  }, [pathname]);

  return (
    <Navbar className="navbar navbar-expand-lg navbar-light bg-light">
      <Container>

        {/* 타이틀 영역 */}
        <Navbar.Brand href="/">Library System</Navbar.Brand>
        {/* 왼쪽 메뉴 */}
        <Nav className="me-auto">
          <Nav.Link href="/">도서 목록</Nav.Link>
          <Form
            className="d-flex align-items-center ms-3"
            action="/search"
            method="GET"
          >
            <Row className="g-1">
              <Col xs="auto">
                <Form.Control
                  type="text"
                  name="keyword" 
                  placeholder="도서 검색"
                  className="me-2 shadow-sm"
                  style={{ borderRadius: "20px", paddingLeft: "16px" }}
                  required  
                />
              </Col>

              <Col xs="auto">
                <Button type="submit" variant="dark" className="px-4" style={{ borderRadius: "20px" }}>
                  검색
                </Button>
              </Col>
            </Row>
          </Form>
        </Nav>

        {/* 오른쪽 Dropdown */}
        <NavDropdown title="내 계정" id="basic-nav-dropdown">
          {isLoggedIn && (
            <>
              <NavDropdown.Item href="/profile">내 정보</NavDropdown.Item>
              <NavDropdown.Item href="/logout">로그아웃</NavDropdown.Item>
              <NavDropdown.Divider />
              <NavDropdown.Item href="/unregister" style={{ color: 'red' }}>
                회원탈퇴
              </NavDropdown.Item>
            </>
          )}
          {!isLoggedIn && (
            <>
              <NavDropdown.Item href="/login">로그인</NavDropdown.Item>
              <NavDropdown.Item href="/signup">회원가입</NavDropdown.Item>
            </>
          )}
        </NavDropdown>

      </Container>
    </Navbar>
  );
}
