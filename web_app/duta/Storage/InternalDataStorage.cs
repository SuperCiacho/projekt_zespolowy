﻿using duta.Storage.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace duta.Storage
{
    public class InternalDataStorage : DataStorage
    {
        private int last_user_id = 0;
        private long last_message_id = 0;
        private List<User> users;
        private List<Message> messages;

        public InternalDataStorage()
        {
            users = new List<User>();
            messages = new List<Message>();

            //test data
            CreateUser("user_a", "pass");
            CreateUser("user_b", "pass");

            User user_a = GetUser("user_a");
            User user_b = GetUser("user_b");

            user_a.contact_list.Add("nick_b", user_b);
        }

        public override User GetUser(int user_id)
        {
            lock (users)
            {
                return users.FirstOrDefault(u => u.user_id == user_id);
            }
        }

        public override User GetUser(string login)
        {
            lock (users)
            {
                return users.FirstOrDefault(u => u.login == login);
            }
        }

        public override List<string> GetUsersWithLoginInContactList(string login)
        {
            lock (users)
            {
                List<string> logins = users.Where(u1 =>
                    null != u1.contact_list.Values.FirstOrDefault(u2 => u2.login == login)).
                    Select(u3 => u3.login).ToList();

                return logins;
            }
        }

        public override int CreateUser(string login, string password)
        {
            lock (users)
            {
                if (GetUser(login) == null)
                {
                    User u = new User(++last_user_id, login, password);
                    users.Add(u);
                    return u.user_id;
                }

                throw new UserAlreadyExistsException();
            }
        }

        public override List<Message> GetMessagesSince(int user, DateTime time)
        {
            lock (messages)
            {
                return messages.Where(m => m.users.Contains(user) && m.time >= time).ToList();
            }
        }

        public override void AddMessage(DateTime time, List<int> msg_users, string message)
        {
            lock (messages)
            {
                foreach (int id in msg_users)
                {
                    if (users.FirstOrDefault(u => u.user_id == id) == null)
                    {
                        throw new UserNotExistingException();
                    }
                }

                messages.Add(new Message(++last_message_id, time, msg_users, message));
            }
        }
    }
}