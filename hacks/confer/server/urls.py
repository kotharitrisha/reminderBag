from django.conf.urls import patterns, include, url

urlpatterns = patterns('',
	url(r'^$','server.views.home'),
	url(r'^home$', 'server.views.home'),
	
	url(r'^data', 'server.views.data'),
    url(r'^recs', 'server.views.get_recs'),
    url(r'^like/(\w+)$', 'server.views.like'),
    url(r'^log/(\w+)$', 'server.views.log'),

    url(r'^login', 'server.views.login'),
    url(r'^register', 'server.views.register'),
    url(r'^logout', 'server.views.logout'),

    url(r'^verify/(.+)$', 'server.views.verify'),
    url(r'^reset/(.+)$', 'server.views.reset'),
    url(r'^verify_email/(.+)$', 'server.views.verify_email'),
    url(r'^reset_email/(.+)$', 'server.views.reset_email'),

    url(r'^error', 'server.views.error'),

    #move this matching in the end
    url(r'^(\w+?)/papers$', 'server.views.papers'),
    url(r'^(\w+?)/schedule$', 'server.views.schedule'),
    url(r'^(\w+?)/paper', 'server.views.paper'),
    url(r'^(\w+)$', 'server.views.conf'),
    url(r'^(\w+?)/$', 'server.views.conf'),
)