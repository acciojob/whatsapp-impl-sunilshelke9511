package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        User user=new User(name,mobile);
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        if(users.size()==2){
            Group gp=new Group(users.get(1).getName(),2);
            groupUserMap.put(gp,users);
            groupMessageMap.put(gp,new ArrayList<>());
            return gp;
        }else{
            customGroupCount++;
            Group gp=new Group("Group "+customGroupCount,users.size());
            groupUserMap.put(gp,users);
            groupMessageMap.put(gp,new ArrayList<>());
            adminMap.put(gp,users.get(0));
            return gp;
        }
    }

    public int createMessage(String content) {
        messageId++;
        Message msg=new Message(messageId,content);
        return msg.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(!groupUserMap.get(group).contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        List<Message> msgList=groupMessageMap.get(group);
        msgList.add(message);
        groupMessageMap.put(group,msgList);

        senderMap.put(message,sender);

        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(adminMap.get(group)!=approver){
            throw new Exception("Approver does not have rights");
        }
        if(!groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        adminMap.replace(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        for(Group g:groupUserMap.keySet()){
            List<User> currGroupUsers=groupUserMap.get(g);
            if(currGroupUsers.contains(user)){
                for(User admin: adminMap.values()){
                    if(user==admin){
                        throw new Exception("Cannot remove admin");
                    }
                }
                groupUserMap.get(g).remove(user);
                for(Message msg:senderMap.keySet()){
                    User u=senderMap.get(msg);
                    if(user==u){
                        senderMap.remove(msg);
                        groupMessageMap.get(g).remove(msg);
                    }
                }
                return groupUserMap.get(g).size()+groupMessageMap.get(g).size()+senderMap.size();
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) {
        Map<Integer,String> map=new HashMap<>();
        List<Integer> list=new ArrayList<>();
        for(Message msg:senderMap.keySet()){
            if(msg.getTimestamp().compareTo(start)>0 && msg.getTimestamp().compareTo(end)<0){
                map.put(msg.getId(), msg.getContent());
                list.add(msg.getId());
            }
        }
        Collections.sort(list);
        k=list.get(list.size()-k);
        return map.get(k);
    }
}