/* tslint:disable:member-ordering */
import {Component, HostListener, OnInit} from '@angular/core';
import {Preview} from '../dto/Preview';
import {ActivatedRoute, Router} from '@angular/router';
import {PreviewService} from '../../service/preview.service';
import {MessengerService} from '../../service/messenger.service';
import {MessageService} from '../../service/message.service';
import {Message} from '../dto/Message';
import {User} from '../dto/User';
import {SearchService} from '../../service/search.service';
import {AuthService} from '../../service/auth.service';
import {CookieService} from '../../service/cookie.service';
import {MessagingService} from '../../service/messaging.service';
import {NewMessage} from '../dto/NewMessage';
import {ConversationService} from '../../service/conversation.service';
import {MessageAttachments} from '../dto/MessageAttachments';
import {SoundNotificationService} from '../../service/sound-notification.service';
import {Title} from '@angular/platform-browser';
import {BackgroundUnreadService} from '../../service/background-unread.service';
import {APP_TITLE, MESSAGES_AMOUNT, MINUTES_AS_ONLINE_LIMIT} from '../../../../globals';

import * as moment from 'moment';

@Component({
  selector: 'app-messaging',
  templateUrl: './messaging.component.html',
  styleUrls: [
    './messaging.component.scss',
    './messaging.component.header-left.scss',
    './messaging.component.header-right.scss',
    './messaging.component.search.scss',
    './messaging.component.select-message.scss',
    './messaging.component.content-left.scss',
    './messaging.component.conversation.scss'
  ]
})
export class MessagingComponent implements OnInit {

  constructor(private route: ActivatedRoute,
              private router: Router,
              private titleService: Title,
              private previewService: PreviewService,
              private messengerService: MessengerService,
              private messageService: MessageService,
              private authService: AuthService,
              private searchService: SearchService,
              private cookieService: CookieService,
              private messagingService: MessagingService,
              private conversationService: ConversationService,
              private soundNotificationService: SoundNotificationService,
              private backgroundUnreadService: BackgroundUnreadService) {
  }

  private token: string;
  isPolling = false;

  me: User;

  previews: Array<Preview>;
  messages: Array<Message>;
  routeConversationId: number;
  currentPreview: Preview;

  messageText = '';

  searchText = '';
  searchUsers: Array<User> = [];
  searchPreviews: Array<Preview> = [];

  selectedMessages: Array<Message> = [];

  editingMessage: Message;
  currentMessageAttachments: MessageAttachments = new MessageAttachments();

  isLeftView = true;
  isSelectForwardTo = false;
  showAttachmentsMenu = false;

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      if (this.editingMessage) {
        this.cancelEditing();
        return;
      }

      if (!this.isSelectForwardTo && this.currentMessageAttachments.forwarded.length !== 0) {
        this.currentMessageAttachments.forwarded = [];
        return;
      }

      if (this.isSelectForwardTo) {
        this.isSelectForwardTo = false;
        this.currentMessageAttachments.forwarded = [];
        return;
      }

      if (this.searchText !== '') {
        this.searchText = '';
        return;
      }

      if (this.selectedMessages.length !== 0) {
        this.deselectMessages();
        return;
      }

      if (this.currentPreview) {
        this.closeConversation();
      }
    }
  }

  @HostListener('window:focus', ['$event'])
  onFocus(event: any): void {
    this.backgroundUnreadService.resetUnreadCount();
    this.titleService.setTitle(APP_TITLE);
  }

  ngOnInit() {
    this.messengerService.oMe.subscribe(me => this.me = me);

    this.route.queryParams.subscribe(params => {
      this.messengerService.oToken.subscribe(token => {
        this.token = token;

        // initial page load issue
        if (this.token === '') {
          return;
        }

        // makes it execute only once per application load
        if (!this.isPolling) {
          this.isPolling = true;
          this.startPolling();
        }

        this.previewService.all(this.token).subscribe(previews => {
          this.previews = previews.filter(p => p.lastMessage);

          this.routeConversationId = params['id'];
          if (this.routeConversationId) {
            this.isLeftView = false;
            this.messageService.get(this.token, this.routeConversationId, 0).subscribe(messages => {
              this.searchText = '';

              this.messages = messages;

              if (messages.length !== 0) {
                this.currentPreview = this.previews.find(p => p.conversation.id == this.routeConversationId);
              } else {
                this.previewService.get(this.token, this.routeConversationId).subscribe(preview => {
                  this.currentPreview = preview;
                });
              }
            });
          } else {
            this.isLeftView = true;
            this.currentPreview = null;
            this.messages = [];
          }
        });
      });
    });
  }

  updatePreviews() {
    this.previewService.all(this.token).subscribe(previews => {
      this.previews = previews;
    });
  }

  updateMessages() {
    this.messageService.get(this.token, this.routeConversationId, 0).subscribe(messages => {
      this.messages = messages;
    });
  }

  // TODO: add debounce support
  previewsOrSearchPreviews(): Array<Preview> {
    if (this.searchText === '') {
      return this.previews;
    } else {
      return this.searchPreviews;
    }
  }

  openConversation(conversationId: number) {
    this.router.navigate(['/im'], {queryParams: {id: conversationId}, replaceUrl: true});
    this.isSelectForwardTo = false;
  }

  closeConversation() {
    this.router.navigate(['/im'], {replaceUrl: true});
  }

  createConversation(user: User) {
    this.conversationService.create(this.token, user.login).subscribe(conversation => {
      this.openConversation(conversation.id);
    });
  }

  searchForConversationsOrUsers() {
    if (this.searchText === '') {
      return;
    }

    if (this.searchText[0] === '@') {
      this.searchService.searchUsers(this.token, this.searchText)
        .subscribe(users => this.searchUsers = users);
    } else {
      this.searchService.searchConversations(this.token, this.searchText)
        .subscribe(conversations => this.searchPreviews = conversations);
    }
  }

  sendMessage() {
    const message = new NewMessage();
    message.senderId = this.me.id;
    message.conversationId = this.currentPreview.conversation.id;
    message.text = this.messageText;
    message.forwarded = this.currentMessageAttachments.forwarded;

    if (message.forwarded.length === 0 && message.text.trim().length === 0) {
      return;
    }

    this.messageText = '';
    this.currentMessageAttachments = new MessageAttachments();

    const tempViewMessage = new Message();
    tempViewMessage.sender = this.me;
    tempViewMessage.forwarded = message.forwarded;
    tempViewMessage.text = message.text;

    this.messages.unshift(tempViewMessage);

    this.messagingService.sendMessage(this.token, message).subscribe(m => {
      this.updatePreviews();
      this.messages = this.messages.filter(mes => mes.id);
      this.messages.unshift(m);
    });
  }

  selectMessage(message: Message) {
    if (!message.selected) {
      message.selected = true;
      this.selectedMessages.push(message);
    } else {
      message.selected = false;
      this.selectedMessages = this.selectedMessages.filter(m => m.id !== message.id);
    }
  }

  deselectMessages() {
    this.selectedMessages.forEach(m => m.selected = false);
    this.selectedMessages = [];
  }

  deleteSelectedMessages() {
    this.messageService.delete(this.token, this.selectedMessages).subscribe(
      success => {
        this.updatePreviews();
        this.updateMessages();
      },
      error => {
        this.updatePreviews();
        this.updateMessages();
      }
    );
    this.deselectMessages();
  }

  editSelectedMessage() {
    this.editingMessage = this.selectedMessages[0];
    this.messageText = this.editingMessage.text;
    setTimeout(() => document.getElementById('send-message-text').focus(), 0);
  }

  editMessage() {
    this.editingMessage.text = this.messageText;

    this.messagingService.editMessage(this.token, this.editingMessage).subscribe(m => {
      this.cancelEditing();
    });
  }

  attachSelectedMessagesAsForwardedAttachment() {
    this.currentMessageAttachments.forwarded = this.selectedMessages;
    this.deselectMessages();
  }

  forwardSelectedMessages() {
    this.attachSelectedMessagesAsForwardedAttachment();
    this.isSelectForwardTo = true;
    this.isLeftView = true;
  }

  removeForwardedAttachment() {
    this.currentMessageAttachments.forwarded = [];
  }

  cancelEditing() {
    this.deselectMessages();
    this.editingMessage = null;
    this.messageText = '';
  }

  logout() {
    this.cookieService.deleteToken();
    this.router.navigate(['/auth'], {replaceUrl: true});
  }

  incrementBackgroundUnread() {
    if (document.visibilityState === 'hidden') {
      this.backgroundUnreadService.incrementUnreadCount();
      this.backgroundUnreadService.oUnreadCount.subscribe(c => {
        this.titleService.setTitle(`${APP_TITLE} ${c} new message${c === 1 ? '' : 's'}`);
      });
    }
  }

  lastSeenView(lastSeen: Date): string {
    if (!lastSeen) {
      return 'offline';
    }

    const now = moment();
    const time = moment(lastSeen);

    const minDiff = now.diff(time, 'minutes');
    if (minDiff < MINUTES_AS_ONLINE_LIMIT) {
      return 'online';
    }
    if (minDiff < 60) {
      return 'seen ' + minDiff + ' minutes ago';
    }
    const hourDiff = now.diff(time, 'hours');
    if (hourDiff < 24) {
      return 'seen today at ' + time.format('hh:mm');
    }
    if (hourDiff < 48) {
      return 'seen tomorrow at ' + time.format('hh:mm');
    }

    return time.format('[seen ] MM Do [ at ] hh:mm');
  }

  isOnline(time: Date): boolean {
    return time && moment().diff(time, 'minutes') < MINUTES_AS_ONLINE_LIMIT;
  }

  loadMore() {
    this.messageService.get(this.token, this.currentPreview.conversation.id, this.messages.length).subscribe(messages => {
      this.messages = this.messages.concat(messages);
    });
  }

  private startPolling() {
    this.getMessage();
    this.getRead();
    this.getEdit();
  }

  private getMessage() {
    this.messagingService.getMessage(this.token).subscribe(
      newMessageAction => {
        this.getMessage();

        this.updatePreviews();
        if (newMessageAction.message.sender.id === this.me.id) {
          return;
        }

        this.incrementBackgroundUnread();
        this.soundNotificationService.notify();
        if (this.currentPreview && newMessageAction.message.conversation.id === this.currentPreview.conversation.id) {
          this.messages.unshift(newMessageAction.message);
        }
      },
      error => {
        this.getMessage();
      }
    );
  }

  private getRead() {
    this.messagingService.getRead(this.token).subscribe(
      conversationReadAction => {
        this.getRead();
        this.updatePreviews();
        if (conversationReadAction.reader.id === this.me.id) {
          return;
        }

        if (this.currentPreview && conversationReadAction.conversation.id === this.currentPreview.conversation.id) {
          this.messages.forEach(m => m.read = true);
        }
      },
      error => {
        this.getRead();
      }
    );
  }

  private getEdit() {
    this.messagingService.getEdit(this.token).subscribe(
      messageEditAction => {
        this.getEdit();
        this.updatePreviews();

        if (this.currentPreview && messageEditAction.message.conversation.id === this.currentPreview.conversation.id) {
          this.messages.filter(m => m.id !== messageEditAction.message.id);
          if (messageEditAction.message.sender.login !== this.me.login) {
            const updatedMessage = this.messages.find(_m => _m.id === messageEditAction.message.id);
            updatedMessage.text = messageEditAction.message.text;
            updatedMessage.forwarded = messageEditAction.message.forwarded;
          }
        }
      },
      error => {
        this.getEdit();
      }
    );
  }
}
