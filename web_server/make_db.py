import os
import sys
from datetime import date
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "poznaika.settings")

from django.contrib.auth.models import User
from django.utils import timezone

import poznaika.accounts.models
from poznaika.accounts.models import Exercise
from poznaika.accounts.models import Course
from poznaika.accounts.models import Mark
from poznaika.accounts.models import License


def AddE(name):
    e = Exercise(Name=name)
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

def AddL(user, start, end):
    l = License(ForUser=user, StartDate=start, EndDate=end)
    l.save()
    return l
    
def MakeInitialDb():
    users = User.objects.filter(username='5')
    if len(users) == 1:
        return # already created
        
    u1 = AddUser('5', '5')
    u2 = AddUser('6', '6')
    
    e1 = AddE("E1")
    e2 = AddE("E2")
    e3 = AddE("E3")
    e4 = AddE("E4")
    
    AddM(u1, e1, 11)
    AddM(u1, e2, 12)
    AddM(u2, e1, 21)
    AddM(u2, e3, 23)
    
    AddL(u1, date(2014, 1, 1), date(2015, 6, 1))
    
    c1 = AddC("C1")
    c2 = AddC("C2")
    
    c1.Exercises.add(e1)
    c1.Exercises.add(e2)
    c1.Exercises.add(e4)
    
    c2.Exercises.add(e1)
    c2.Exercises.add(e3)
    c2.Exercises.add(e4)

    
MakeInitialDb()

License.objects.all().delete()
u = User.objects.get(username="5")
AddL(u, date(2014, 1, 1), date(2015, 1, 1))