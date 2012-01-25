/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.osgi.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.osgi.ApplicationReference;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rwt.application.*;
import org.eclipse.rwt.branding.AbstractBranding;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.*;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;


public class ApplicationLauncherImpl_Test extends TestCase {
  
  private static final String CONTEXT_NAME = "context";
  private static final String FILTER_EXPRESSION = "(key=value)";
  private static final String SERVLET_ALIAS_1 = "servlet1";
  private static final String SERVLET_ALIAS_2 = "servlet2";

  private BundleContext bundleContext;
  private HttpService httpService;
  private ServiceReference< HttpService > httpServiceReference;
  private ApplicationConfigurator configurator;
  private ServiceReference< ApplicationConfigurator > configuratorReference;
  private ApplicationLauncherImpl applicationLauncher;
  private ServiceRegistration serviceRegistration;
  private LogService log;
  
  public void testLaunch() {
    String path = Fixture.WEB_CONTEXT_DIR.getPath();
    
    applicationLauncher.launch( configurator, httpService, null, null, path );
    
    checkDefaultAliasHasBeenRegistered();
    checkWebContextResourcesHaveBeenCreated();
    checkHttpContextHasBeenCreated();
    checkApplicationReferenceHasBeenRegisteredAsService();
  }

  public void testLaunchWithHttpContext() {
    HttpContext httpContext = mock( HttpContext.class );
    String path = Fixture.WEB_CONTEXT_DIR.getPath();
    applicationLauncher.launch( configurator, httpService, httpContext, null, path );
    
    checkDefaultAliasHasBeenRegistered();
    checkWebContextResourcesHaveBeenCreated();
    checkHttpContextHasBeenWrapped();
    checkApplicationReferenceHasBeenRegisteredAsService();
  }
  
  public void testLaunchWithDefaultContextDirectory() {
    launchApplication();
    
    checkDefaultAliasHasBeenRegistered();
    checkWebContextResourcesHaveBeenCreated();
  }
  
  public void testLaunchWithProblem() {
    prepareConfiguratorToThrowException();
    mockLogService();
    
    registerServiceReferences();
    
    checkProblemHasBeenLogged();
  }

  public void testStopApplication() {
    String path = Fixture.WEB_CONTEXT_DIR.getPath();
    ApplicationReference context = launchApplicationReference( path );
    
    context.stopApplication();

    checkDefaultAliasHasBeenUnregistered();
    checkWebContextResourcesHaveBeenDeleted();
    checkApplicationReferenceHasBeenUnregisteredAsService();
  }
  
  public void testStopApplicationReferenceWithProblem() {
    mockLogService();
    ApplicationReferenceImpl applicationReference = createMalignApplicationReference();
    
    applicationLauncher.stopApplicationReference( applicationReference );
    
    checkProblemHasBeenLogged();
  }
  
  public void testActivationStateAfterDeactivation() {
    ApplicationReference applicationReference = launchApplication();

    applicationLauncher.deactivate();

    checkDeactivateStateOfApplicationReference( applicationReference );
    checkDeactivatedStateOfApplicationLauncher();
    checkDefaultAliasHasBeenUnregistered();
    checkWebContextResourcesHaveBeenDeleted();
  }
  
  public void testLaunchWithMultipleServletNames() {
    createAliasConfigurator( SERVLET_ALIAS_1, SERVLET_ALIAS_2 );
    createApplicationLauncher();
    
    launchApplication();
    
    checkAliasHasBeenRegistered( SERVLET_ALIAS_1 );
    checkAliasHasBeenRegistered( SERVLET_ALIAS_2 );
  }
  
  public void testStopApplicationWithMultipleServletNames() {
    createAliasConfigurator( SERVLET_ALIAS_1, SERVLET_ALIAS_2 );
    createApplicationLauncher();
    ApplicationReference applicationReference = launchApplication();

    applicationReference.stopApplication();
    
    checkAliasHasBeenUnregistered( SERVLET_ALIAS_1 );
    checkAliasHasBeenUnregistered( SERVLET_ALIAS_2 );
  }
  
  public void testLaunchWithContextName() {
    mockBundleContext( CONTEXT_NAME );
    createApplicationLauncher();
    String location = applicationLauncher.getLocation( CONTEXT_NAME, configurator, httpService );
    
    applicationLauncher.launch( configurator, httpService, null, CONTEXT_NAME, location );
    
    checkAliasHasBeenRegistered( CONTEXT_NAME + "/" + ApplicationReferenceImpl.DEFAULT_ALIAS );
  }
  
  public void testStopApplicationWithContextName() {
    mockBundleContext( CONTEXT_NAME );
    createApplicationLauncher();
    String location = applicationLauncher.getLocation( CONTEXT_NAME, configurator, httpService );
    ApplicationReference applicationReference
      = applicationLauncher.launch( configurator, httpService, null, CONTEXT_NAME, location );
    
    applicationReference.stopApplication();
    
    checkAliasHasBeenUnregistered( CONTEXT_NAME + "/" + ApplicationReferenceImpl.DEFAULT_ALIAS );
  }
  
  public void testActivate() {
    registerServiceReferences();
    
    checkDefaultAliasHasBeenRegistered();
    checkWebContextResourcesHaveBeenCreated();
  }
  
  public void testDeactivate() {
    ApplicationReferenceImpl applicationreference
      = ( ApplicationReferenceImpl )launchApplication();
    
    applicationLauncher.deactivate();
    
    assertFalse( applicationreference.isAlive() );
  }
  
  public void testAddConfigurator() {
    applicationLauncher.addHttpService( httpServiceReference );
    
    ApplicationConfigurator added = applicationLauncher.addConfigurator( configuratorReference );

    assertSame( configurator, added );
    checkDefaultAliasHasBeenRegistered();
    checkWebContextResourcesHaveBeenCreated();
  }
  
  public void testRemoveConfigurator() {
    applicationLauncher.addHttpService( httpServiceReference );
    applicationLauncher.addConfigurator( configuratorReference );

    applicationLauncher.removeConfigurator( configurator );
    
    checkDefaultAliasHasBeenUnregistered();
    checkWebContextResourcesHaveBeenDeleted();
  }
  
  public void testAddHttpService() {
    applicationLauncher.addConfigurator( configuratorReference );
    
    HttpService added = applicationLauncher.addHttpService( httpServiceReference );
    
    assertSame( httpService, added );
    checkDefaultAliasHasBeenRegistered();
    checkWebContextResourcesHaveBeenCreated();
  }
  
  public void testRemoveHttpService() {
    ApplicationReferenceImpl reference1 = ( ApplicationReferenceImpl )launchApplication();
    ApplicationReferenceImpl reference2 = ( ApplicationReferenceImpl )launchApplication();
    
    applicationLauncher.removeHttpService( httpService );
    
    assertFalse( reference1.isAlive() );
    assertFalse( reference2.isAlive() );
    checkWebContextResourcesHaveBeenDeleted();
  }
  
  public void testAddConfigurerAfterLaunch() {
    ApplicationReference reference = launchApplication();
    applicationLauncher.addHttpService( httpServiceReference );
    reference.stopApplication();
    
    mockSecondConfiguratorReference();
    
    checkDefaultAliasHasBeenRegisteredTwice();
  }
  
  public void testNonMatchingFilterUsageHttpService() {
    configureHttpServiceFilter( "wrongValue" );
    
    registerServiceReferences();
    
    checkDefaultAliasHasNotBeenRegistered();
  }

  public void testNonMatchingFilterUsageConfigurator() {
    configureConfiguratorFilter( "wrongValue" );
    
    registerServiceReferences();
    
    checkDefaultAliasHasNotBeenRegistered();
  }
  
  public void testMatchingFilterUsageHttpService() {
    configureHttpServiceFilter( "value" );
    
    registerServiceReferences();
    
    checkDefaultAliasHasBeenRegistered();
  }
  
  public void testMatchingFilterUsageConfigurator() {
    configureConfiguratorFilter( "value" );

    registerServiceReferences();
    
    checkDefaultAliasHasBeenRegistered();
  }

  protected void setUp() {
    Fixture.deleteWebContextDirectory();
    Fixture.setIgnoreResourceDeletion( false );
    Fixture.useTestResourceManager();
    mockConfigurator();
    mockHttpService();
    mockBundleContext();
    createApplicationLauncher();
  }

  protected void tearDown() {
    Fixture.delete( Fixture.WEB_CONTEXT_DIR );
    Fixture.setIgnoreResourceDeletion( Fixture.usePerformanceOptimizations() );
  }

  @SuppressWarnings( "unchecked" )
  private ServiceRegistration< ? > checkApplicationReferenceHasBeenRegisteredAsService() {
    return verify( bundleContext ).registerService( eq( ApplicationReference.class.getName() ),
                                             any( ApplicationReference.class ),
                                             any( Dictionary.class ) );
  }

  private void checkApplicationReferenceHasBeenUnregisteredAsService() {
    verify( serviceRegistration ).unregister();
  }

  private void checkDefaultAliasHasBeenRegisteredTwice() {
    checkAliasHasBeenRegistered( ApplicationReferenceImpl.DEFAULT_ALIAS, 2 );
  }

  private void checkDefaultAliasHasNotBeenRegistered() {
    checkAliasHasBeenRegistered( ApplicationReferenceImpl.DEFAULT_ALIAS, 0 );
  }
  
  private void checkDefaultAliasHasBeenRegistered() {
    checkAliasHasBeenRegistered( ApplicationReferenceImpl.DEFAULT_ALIAS, 1 );
  }

  private void checkAliasHasBeenRegistered( String alias ) {
    checkAliasHasBeenRegistered( alias, 1 );
  }
  
  private void checkAliasHasBeenRegistered( String alias, int times )  {
    try {
      verify( httpService, times( times ) ).registerServlet( eq( "/" + alias ),
                                                             any( HttpServlet.class ), 
                                                             any( Dictionary.class ), 
                                                             any( HttpContext.class ) );
      verify( httpService, times( times ) ).registerResources( eq( getResourcesDirectory( alias ) ),
                                                               any( String.class ), 
                                                               any( HttpContext.class ) );
    } catch( Exception shouldNotHappen ) {
      throw new RuntimeException( shouldNotHappen );
    }
  }

  private String getResourcesDirectory( String alias ) {
    String result = "/" + Application.RESOURCES;
    if( alias.contains( "/" ) ) {
      result = "/" + CONTEXT_NAME + "/" + Application.RESOURCES;
    }
    return result;
  }
  
  private void checkDefaultAliasHasBeenUnregistered() {
    checkAliasHasBeenUnregistered( ApplicationReferenceImpl.DEFAULT_ALIAS );
  }

  private void checkAliasHasBeenUnregistered( String alias ) {
    verify( httpService ).unregister( "/" + alias );
    verify( httpService ).unregister( getResourcesDirectory( alias ) );
  }
  
  private void checkWebContextResourcesHaveBeenCreated() {
    assertTrue( Fixture.WEB_CONTEXT_RWT_RESOURCES_DIR.exists() );
  }
  
  private void checkWebContextResourcesHaveBeenDeleted() {
    assertFalse( Fixture.WEB_CONTEXT_RWT_RESOURCES_DIR.exists() );
  }

  private void checkDeactivateStateOfApplicationReference( ApplicationReference reference ) {
    assertFalse( ( ( ApplicationReferenceImpl )reference ).isAlive() );
    reference.stopApplication(); // check that repeatedly calls to stop do not cause any problems
  }

  private void checkDeactivatedStateOfApplicationLauncher() {
    assertFalse( applicationLauncher.isAlive() );
    assertNull( applicationLauncher.launch( configurator, httpService, null, null, "/contextPath" ) );
  }

  private HttpContext checkHttpContextHasBeenWrapped() {
    return verify( httpService, never() ).createDefaultHttpContext();
  }
  
  private void checkHttpContextHasBeenCreated() {
    verify( httpService ).createDefaultHttpContext();
  }

  private void checkProblemHasBeenLogged() {
    verify( log ).log( eq( LogService.LOG_ERROR ),
                       any( String.class ), 
                       any( IllegalStateException.class ) );
  }

  @SuppressWarnings( "unchecked" )
  private void mockLogService() {
    log = mock( LogService.class );
    ServiceReference logReference = mock( ServiceReference.class );
    when( bundleContext.getServiceReference( LogService.class.getName() ) )
      .thenReturn( logReference );
    when( bundleContext.getService( logReference ) ).thenReturn( log );
  }

  private void registerServiceReferences() {
    applicationLauncher.addConfigurator( configuratorReference );
    applicationLauncher.addHttpService( httpServiceReference );
  }
  
  private void configureConfiguratorFilter( String value ) {
    Class< ? > targetType = HttpService.class;
    ServiceReference< ?> serviceReference = configuratorReference;
    ServiceReference< ? > targetReference = httpServiceReference;
    configureFilterScenario( value, targetType, serviceReference, targetReference );
  }

  private void configureHttpServiceFilter( String value ) {
    Class< ? > targetType = ApplicationConfigurator.class;
    ServiceReference< ?> serviceReference = httpServiceReference;
    ServiceReference< ? > targetReference = configuratorReference;
    configureFilterScenario( value, targetType, serviceReference, targetReference );
  }

  private void configureFilterScenario( String value,
                                        Class< ? > targetType,
                                        ServiceReference< ? > serviceReference,
                                        ServiceReference< ? > targetReference )
  {
    String target = Matcher.createTargetKey( targetType );
    when( serviceReference.getProperty( target ) ).thenReturn( FILTER_EXPRESSION );
    when( targetReference.getProperty( "key" ) ).thenReturn( value );
  }

  private void createApplicationLauncher() {
    applicationLauncher = new ApplicationLauncherImpl( bundleContext );
  }

  @SuppressWarnings( "unchecked" )
  private void mockHttpServiceReference() {
    httpServiceReference =  mock( ServiceReference.class );
  }

  private void mockConfigurator() {
    configurator = mock( ApplicationConfigurator.class );
    mockConfiguratorReference();
  }

  @SuppressWarnings( "unchecked" )
  private void mockConfiguratorReference() {
    configuratorReference = mock( ServiceReference.class );
  }

  private void mockSecondConfiguratorReference() {
    mockConfiguratorReference();
    configurator = mock( ApplicationConfigurator.class );
    when( bundleContext.getService( configuratorReference ) ).thenReturn( configurator );
    when( bundleContext.getDataFile( any( String.class ) ) ).thenReturn( Fixture.WEB_CONTEXT_DIR );
    applicationLauncher.addConfigurator( configuratorReference );
  }

  
  private void createAliasConfigurator( final String alias1, final String alias2 ) {
    configurator = new ApplicationConfigurator() {
      public void configure( ApplicationConfiguration configuration ) {
        configuration.addBranding( mockBranding( alias1 ) );
        configuration.addBranding( mockBranding( alias2 ) );
      }
    };
    mockBundleContext();
  }

  private void mockHttpService() {
    httpService = mock( HttpService.class );
    mockServletConfigForServletContextRetrieval( httpService );
    mockHttpServiceReference();
  }

  private void mockServletConfigForServletContextRetrieval( HttpService service ) {
    String servletContextFinderAlias = ApplicationReferenceImpl.SERVLET_CONTEXT_FINDER_ALIAS;
    String alias1 = "/" + servletContextFinderAlias;
    String alias2 = "/" + CONTEXT_NAME + "/" + servletContextFinderAlias;
    mockServletConfigForServletContextRetrieval( service, alias1 );
    mockServletConfigForServletContextRetrieval( service, alias2 );
  }

  private void mockServletConfigForServletContextRetrieval( HttpService service, String alias ) {
    try {
      doAnswer( mockServletConfigForServletContextRetrieval() )
       .when( service ).registerServlet( eq( alias ),
                                        any( HttpServlet.class ),
                                        any( Dictionary.class ),
                                        any( HttpContext.class ) );
    } catch( Exception shouldNotHappen ) {
      throw new RuntimeException( shouldNotHappen );
    }
  }

  private Answer mockServletConfigForServletContextRetrieval() {
    return new Answer() {
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        HttpServlet servlet = ( HttpServlet )invocation.getArguments()[ 1 ];
        mockServletConfigForServletContextRetrieval( servlet );
        return null;
      }
    };
  }

  private void mockServletConfigForServletContextRetrieval( HttpServlet servlet ) {
    ServletConfig servletConfig = mock( ServletConfig.class );
    // use the fixture servlet context for performanc optimizations
    ServletContext servletContext = Fixture.createServletContext();
    when( servletConfig.getServletContext() ).thenReturn( servletContext );
    initServlet( servlet, servletConfig );
  }

  private void initServlet( HttpServlet servlet, ServletConfig servletConfig ) {
    try {
      servlet.init( servletConfig );
    } catch( ServletException shouldNotHappen ) {
      throw new RuntimeException( shouldNotHappen );
    }
  }

  private void mockBundleContext() {
    mockBundleContext( null );
  }

  @SuppressWarnings( "unchecked" )
  private void mockBundleContext( String contextName ) {
    bundleContext = mock( BundleContext.class );
    String name = ApplicationLauncherImpl.getContextFileName( contextName, configurator, httpService );
    when( bundleContext.getDataFile( eq( name ) ) ).thenReturn( Fixture.WEB_CONTEXT_DIR );
    when( bundleContext.getService( httpServiceReference ) ).thenReturn( httpService );
    when( bundleContext.getService( configuratorReference ) ).thenReturn( configurator );
    serviceRegistration = mock( ServiceRegistration.class );
    when( bundleContext.registerService( eq( ApplicationReference.class.getName() ), 
                                         any( ApplicationReference.class ),
                                         any( Dictionary.class ) ) )
      .thenReturn( serviceRegistration );
  }

  private AbstractBranding mockBranding( String servletName ) {
    AbstractBranding result = mock( AbstractBranding.class );
    when( result.getServletName() ).thenReturn( servletName );
    return result;
  }
  
  private ApplicationReference launchApplication() {
    String location = applicationLauncher.getLocation( null, configurator, httpService );
    return launchApplicationReference( location );
  }

  private ApplicationReference launchApplicationReference( String location ) {
    return applicationLauncher.launch( configurator, httpService, null, null, location );
  }
  
  private void prepareConfiguratorToThrowException() {
    doThrow( new IllegalStateException() )
      .when( configurator ).configure( any( ApplicationConfiguration.class ) );
  }

  private ApplicationReferenceImpl createMalignApplicationReference() {
    ApplicationReferenceImpl result = mock( ApplicationReferenceImpl.class );
    doThrow( new IllegalStateException() ).when( result ).stopApplication();
    return result;
  }
}