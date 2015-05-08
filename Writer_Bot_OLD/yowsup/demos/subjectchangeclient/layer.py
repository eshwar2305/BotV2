from yowsup.layers.interface                           import YowInterfaceLayer, ProtocolEntityCallback
from yowsup.layers.protocol_messages.protocolentities  import TextMessageProtocolEntity
from yowsup.layers.protocol_groups.protocolentities      import *
import threading
import logging
logger = logging.getLogger(__name__)

class SetSubjectLayer(YowInterfaceLayer):

    PROP_MESSAGES = "org.openwhatsapp.yowsup.prop.sendclient.queue" #list of (jid, message) tuples

    def __init__(self):
        super(SetSubjectLayer, self).__init__()
        self.ackQueue = []
        self.lock = threading.Condition()

    # @ProtocolEntityCallback("success")
    # def onSuccess(self, successProtocolEntity):
    #    self.lock.acquire()
    #    for target in self.getProp(self.__class__.PROP_MESSAGES, []):
    #        phone, message = target
    #        if '@' in phone:
    #            messageEntity = TextMessageProtocolEntity(message, to = phone)
    #        elif '-' in phone:
    #            messageEntity = TextMessageProtocolEntity(message, to = "%s@g.us" % phone)
    #        else:
    #            messageEntity = TextMessageProtocolEntity(message, to = "%s@s.whatsapp.net" % phone)
    #        self.ackQueue.append(messageEntity.getId())
    #        self.toLower(messageEntity)
    #    self.lock.release()

    @ProtocolEntityCallback("ack")
    def onAck(self, entity):
        self.lock.acquire()
        if entity.getId() in self.ackQueue:
            self.ackQueue.pop(self.ackQueue.index(entity.getId()))

        if not len(self.ackQueue):
            self.lock.release()
            logger.info("Message sent")
            raise KeyboardInterrupt()

        self.lock.release()

    @ProtocolEntityCallback("success")
    def group_setSubject(self, successProtocolEntity):
        self.lock.acquire()
        for target in self.getProp(self.__class__.PROP_MESSAGES, []):
            phone, message = target
            entity = SubjectGroupsIqProtocolEntity("%s@g.us" % phone,message)
            logger.info("set subject entity ready")
            self.toLower(entity)
            logger.info("set subject entity sent to lower")
            raise KeyboardInterrupt()
        self.lock.release()
