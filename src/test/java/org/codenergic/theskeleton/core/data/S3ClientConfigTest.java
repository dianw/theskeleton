/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenergic.theskeleton.core.data;

import io.minio.MinioClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class S3ClientConfigTest {
	private S3ClientConfig s3ClientConfig = new S3ClientConfig();
	private S3ClientConfig.S3ClientProperties s3ClientProperties = new S3ClientConfig.S3ClientProperties();
	@Mock
	private MinioClient minioClient;
	@Mock
	private ScheduledExecutorService executorService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		s3ClientProperties.getBuckets().add("test1");
		s3ClientProperties.getBuckets().add("test2");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateBucket() throws Exception {
		ArgumentCaptor<Callable<List<String>>> argumentCaptor = ArgumentCaptor.forClass(Callable.class);
		when(executorService.schedule(argumentCaptor.capture(), anyLong(), any())).then(invocation -> {
			Callable<List<String>> callable = invocation.getArgument(0);
			callable.call();
			return null;
		});
		when(minioClient.bucketExists(eq("test1"))).thenReturn(true);
		when(minioClient.bucketExists(eq("test2"))).thenReturn(false);
		s3ClientConfig.createBuckets(minioClient, executorService, s3ClientProperties);
		verify(minioClient, times(2)).bucketExists(anyString());
		verify(minioClient).makeBucket(eq("test2"));
		verify(executorService).schedule(argumentCaptor.capture(), anyLong(), any());
		when(minioClient.bucketExists(anyString())).thenThrow(Exception.class);
		s3ClientConfig.createBuckets(minioClient, executorService, s3ClientProperties);
	}
}
