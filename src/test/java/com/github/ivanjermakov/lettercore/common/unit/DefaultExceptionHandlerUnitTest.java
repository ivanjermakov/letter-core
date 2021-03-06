package com.github.ivanjermakov.lettercore.common.unit;

import com.github.ivanjermakov.lettercore.auth.exception.AuthenticationException;
import com.github.ivanjermakov.lettercore.common.exception.NoSuchEntityException;
import com.github.ivanjermakov.lettercore.common.exception.WebException;
import com.github.ivanjermakov.lettercore.config.DefaultExceptionHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class DefaultExceptionHandlerUnitTest {

	@Autowired
	private DefaultExceptionHandler exceptionHandler;

	private HttpServletRequest request;

	@Before
	public void before() {
		request = mock(HttpServletRequest.class);
	}

	@Test
	public void shouldHandleAnyException() {
		WebException webException = exceptionHandler.handleAnyException(request, new Exception());

		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.valueOf(webException.status));
	}

	@Test
	public void shouldHandleAuthenticationException() {
		WebException webException = exceptionHandler.handleAuthenticationException(request, new AuthenticationException());

		Assert.assertEquals(HttpStatus.UNAUTHORIZED, HttpStatus.valueOf(webException.status));
	}

	@Test
	public void shouldHandleNoSuchEntityException() {
		WebException webException = exceptionHandler.handleNoSuchEntityException(request, new NoSuchEntityException());

		Assert.assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(webException.status));
	}

}
