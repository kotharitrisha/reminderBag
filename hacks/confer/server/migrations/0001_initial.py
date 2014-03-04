# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'Conference'
        db.create_table('conferences', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('unique_name', self.gf('django.db.models.fields.CharField')(unique=True, max_length=50)),
            ('confer_name', self.gf('django.db.models.fields.CharField')(unique=True, max_length=50)),
            ('title', self.gf('django.db.models.fields.TextField')()),
            ('subtitle', self.gf('django.db.models.fields.TextField')()),
            ('blurb', self.gf('django.db.models.fields.TextField')()),
            ('location', self.gf('django.db.models.fields.CharField')(max_length=100)),
            ('start_date', self.gf('django.db.models.fields.DateField')()),
            ('end_date', self.gf('django.db.models.fields.DateField')()),
        ))
        db.send_create_signal('server', ['Conference'])

        # Adding model 'User'
        db.create_table('users', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('timestamp', self.gf('django.db.models.fields.DateTimeField')(auto_now=True, blank=True)),
            ('email', self.gf('django.db.models.fields.CharField')(unique=True, max_length=100)),
            ('f_name', self.gf('django.db.models.fields.CharField')(max_length=50)),
            ('l_name', self.gf('django.db.models.fields.CharField')(max_length=50)),
            ('password', self.gf('django.db.models.fields.CharField')(max_length=500)),
        ))
        db.send_create_signal('server', ['User'])

        # Adding model 'Registration'
        db.create_table('registrations', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('timestamp', self.gf('django.db.models.fields.DateTimeField')(auto_now=True, blank=True)),
            ('user', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['server.User'])),
            ('conference', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['server.Conference'])),
        ))
        db.send_create_signal('server', ['Registration'])

        # Adding model 'Likes'
        db.create_table('likes', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('timestamp', self.gf('django.db.models.fields.DateTimeField')(auto_now=True, blank=True)),
            ('registration', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['server.Registration'])),
            ('likes', self.gf('django.db.models.fields.TextField')()),
        ))
        db.send_create_signal('server', ['Likes'])

        # Adding model 'Logs'
        db.create_table('logs', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('timestamp', self.gf('django.db.models.fields.DateTimeField')(auto_now=True, blank=True)),
            ('registration', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['server.Registration'])),
            ('action', self.gf('django.db.models.fields.CharField')(max_length=50)),
            ('data', self.gf('django.db.models.fields.TextField')()),
        ))
        db.send_create_signal('server', ['Logs'])


    def backwards(self, orm):
        # Deleting model 'Conference'
        db.delete_table('conferences')

        # Deleting model 'User'
        db.delete_table('users')

        # Deleting model 'Registration'
        db.delete_table('registrations')

        # Deleting model 'Likes'
        db.delete_table('likes')

        # Deleting model 'Logs'
        db.delete_table('logs')


    models = {
        'server.conference': {
            'Meta': {'ordering': "['-start_date']", 'object_name': 'Conference', 'db_table': "'conferences'"},
            'blurb': ('django.db.models.fields.TextField', [], {}),
            'confer_name': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '50'}),
            'end_date': ('django.db.models.fields.DateField', [], {}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'location': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            'start_date': ('django.db.models.fields.DateField', [], {}),
            'subtitle': ('django.db.models.fields.TextField', [], {}),
            'title': ('django.db.models.fields.TextField', [], {}),
            'unique_name': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '50'})
        },
        'server.likes': {
            'Meta': {'object_name': 'Likes', 'db_table': "'likes'"},
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'likes': ('django.db.models.fields.TextField', [], {}),
            'registration': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['server.Registration']"}),
            'timestamp': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'})
        },
        'server.logs': {
            'Meta': {'ordering': "['-timestamp']", 'object_name': 'Logs', 'db_table': "'logs'"},
            'action': ('django.db.models.fields.CharField', [], {'max_length': '50'}),
            'data': ('django.db.models.fields.TextField', [], {}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'registration': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['server.Registration']"}),
            'timestamp': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'})
        },
        'server.registration': {
            'Meta': {'object_name': 'Registration', 'db_table': "'registrations'"},
            'conference': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['server.Conference']"}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'timestamp': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'}),
            'user': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['server.User']"})
        },
        'server.user': {
            'Meta': {'object_name': 'User', 'db_table': "'users'"},
            'email': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '100'}),
            'f_name': ('django.db.models.fields.CharField', [], {'max_length': '50'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'l_name': ('django.db.models.fields.CharField', [], {'max_length': '50'}),
            'password': ('django.db.models.fields.CharField', [], {'max_length': '500'}),
            'timestamp': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'})
        }
    }

    complete_apps = ['server']