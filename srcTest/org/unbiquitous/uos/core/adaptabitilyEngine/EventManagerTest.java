package org.unbiquitous.uos.core.adaptabitilyEngine;

import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

public class EventManagerTest {

	private MessageEngine engine;
	private EventManager manager;
	private UosEventListener listener;
	private ArgumentCaptor<ServiceCall> call;

	@Before
	public void setUp() throws MessageEngineException {
		engine = mock(MessageEngine.class);
		when(engine.callService((UpDevice)any(), (ServiceCall)any()))
		.thenReturn(new ServiceResponse());
		manager = new EventManager(engine);
		listener = mock(UosEventListener.class);
		call = ArgumentCaptor.forClass(ServiceCall.class);
	}
	
	@Test
	public void registeringDelegatesToMessageEngine() throws Exception{
		UpDevice device = new UpDevice("the_device");
		
		manager.registerForEvent(listener, device, "driver", "id", "key");
		
		verify(engine).callService(eq(device), call.capture());
		assertThat(call.getAllValues()).hasSize(1);
		assertThat(call.getValue().getService()).isEqualTo("registerListener");
	}
	
	@Test
	public void registeringDontDelegatesForNullDevice() throws Exception{
		manager.registerForEvent(listener, null, "driver", "id", "key");
		
		verify(engine,never()).callService((UpDevice)any(),(ServiceCall)any());
	}
	
	@Test
	public void notifyDelegatesToMessageEngine() throws Exception{
		UpDevice device = new UpDevice("the_device");
		
		Notify notify = new Notify("a");
		manager.sendEventNotify(notify, device);
		
		verify(engine).notifyEvent(eq(notify),eq(device));
	}
	
	@Test
	public void notifiesToThelistenerWhenDeviceIsNull() throws Exception{
		manager.registerForEvent(listener, null, "driver", "id", "key");
		
		Notify notify = new Notify("key","driver","id");
		manager.sendEventNotify(notify, null);
		
		verify(listener).handleEvent(eq(notify));
	}
	
	//TODO: test other combinations of driver/id/key

	@Test
	@Ignore
	public void unregisteringDelegatesToMessageEngine() throws Exception{
		UpDevice device = new UpDevice("the_device");
		
		manager.registerForEvent(listener, device, "driver", "id", "key");
		manager.unregisterForEvent(listener, device, "driver", "id", "key");
		
		verify(engine).callService(eq(device), call.capture());
		assertThat(call.getAllValues()).hasSize(2);
		assertThat(call.getAllValues().get(1).getService()).isEqualTo("unregisterListener");
	}
	
	@Test
	@Ignore
	public void dontFailWehnUnregisteringWithoutRegistering() throws Exception{
		UpDevice device = new UpDevice("the_device");
		
		manager.unregisterForEvent(listener, device, "driver", "id", "key");
		
		verify(engine).callService(eq(device), call.capture());
		assertThat(call.getAllValues()).hasSize(2);
		assertThat(call.getAllValues().get(1).getService()).isEqualTo("unregisterListener");
	}
	
}
