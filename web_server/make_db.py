# coding=cp1251

import os
import sys
from datetime import date
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "poznaika.settings")

from django.contrib.auth.models import User
from django.contrib.auth.management.commands import createsuperuser
from django.utils import timezone

import poznaika.accounts.models
from poznaika.accounts.models import Exercise
from poznaika.accounts.models import Course
from poznaika.accounts.models import Mark
from poznaika.accounts.models import License
from poznaika.accounts.models import StudyHead
from poznaika.accounts.models import Teacher
from poznaika.accounts.models import Class
from poznaika.accounts.models import Pupil


def AddE(name, descr=""):
    e = Exercise(Name=name, Description=descr)
    e.save()
    return e

def AddC(name):
    c = Course(Name=name)
    c.save()
    return c
    
def AddM(user, exe, points):
    m = Mark(ForUser=user, ForExercise=exe, Score=points, DateTime=timezone.now())
    m.save()
    return m
    
def AddUser(name, pwd):
    u = User.objects.create_user(name, name+'@mail.com', pwd)
    u.save()
    return u

def AddL(user, start, end, count=1):
    l = License(ForUser=user, StartDate=start, EndDate=end, Count=count)
    l.save()
    return l

def AddPupil(user, clss):
    p = Pupil(User=user, ForClass=clss)
    p.save()
    return p

    
def MakeInitialDb():
    User.objects.all().delete()
    Exercise.objects.all().delete()
    Course.objects.all().delete()
        
    #u1 = AddUser(u'5', u'5')
    u1 = AddUser(u'���� ���������', '123')
    u2 = AddUser(u'���� �������', '123')
    u22 = AddUser(u'��� ��������', '123')
    u3 = AddUser(u'���� ������', '123')
    u4 = AddUser(u'���� ��������', '123')
    u5 = AddUser(u'���� ��������', '123')
    uH = AddUser(u'kino_school', '12345')
    uT1 = AddUser(u'��������� ��', '12345')
    uT2 = AddUser(u'������ ����', '12345')
    
    h = StudyHead(ForUser=uH, Description="Schools from movies")
    h.save()
    
    t = Teacher(User=uT1, ForHead=h, Description=u"��� / 100���")
    t.save()
    t = Teacher(User=uT2, ForHead=h, Description=u"����")
    t.save()
    
    c1 = Class(ForHead=h, Name="1A")
    c1.save()
    c2 = Class(ForHead=h, Name="1B")
    c2.save()

    AddPupil(u1, c1)
    AddPupil(u2, c1)
    AddPupil(u22, c1)
    AddPupil(u3, c2)
    AddPupil(u4, c2)
    AddPupil(u5, c2)
    
    e1 = AddE("E1")
    e2 = AddE("E2", u"����� ���������")
    e3 = AddE("E3", u"��� ����� �����-������")
    e4 = AddE("E4")
    
    e5 = AddE("Alphabet.Russian.WordGather", u"����� �����������")
    
    AddM(u1, e1, 11)
    AddM(u1, e2, 12)
    AddM(u1, e2, 122)
    AddM(u2, e1, 21)
    AddM(u2, e3, 23)
    
    AddL(u1, date(2015, 1, 1), date(2016, 1, 1), 1)
    AddL(uH, date(2015, 1, 1), date(2016, 1, 1), 100)
    
    c1 = AddC("C1")
    c2 = AddC("C2")
    
    c1.Exercises.add(e1)
    c1.Exercises.add(e2)
    c1.Exercises.add(e4)
    
    c2.Exercises.add(e1)
    c2.Exercises.add(e3)
    c2.Exercises.add(e4)

    
MakeInitialDb()
print("Done.")
