import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {AutosizeModule} from 'ngx-autosize';
import {ArraySortPipeAsc, ArraySortPipeDesc} from './pipe/array-sort.pipe';
import {AuthComponent} from './component/routed/auth/auth.component';
import {RegisterComponent} from './component/routed/register/register.component';
import {ImageAttachmentComponent} from './component/embedded/attachment/image-attachment/image-attachment.component';
import {ProfileComponent} from './component/embedded/profile/profile.component';
import {ForwardedAttachmentComponent} from './component/embedded/attachment/forwarded-attachment/forwarded-attachment.component';
import {OverlayClickDirective} from './component/routed/messaging/overlay-click.directive';
import {OutsideClickDirective} from './component/routed/messaging/outside-click.directive';
import {ScrollTopDirective} from './component/routed/messaging/scroll-top.directive';
import {ShowAttachmentsMenuDirective} from './component/routed/messaging/show-attachments-menu.directive';
import {MessageSendDirective} from './component/routed/messaging/message-send.directive';
import {MessageComponent} from './component/embedded/message/message.component';
import {PreviewComponent} from './component/embedded/preview/preview.component';
import {MessagingComponent} from './component/routed/messaging/messaging.component';

@NgModule({
	declarations: [
		AppComponent,
		AuthComponent,
		RegisterComponent,
		MessagingComponent,
		PreviewComponent,
		MessageComponent,
		MessageSendDirective,
		ShowAttachmentsMenuDirective,
		ScrollTopDirective,
		OutsideClickDirective,
		OverlayClickDirective,
		ArraySortPipeAsc,
		ArraySortPipeDesc,
		ForwardedAttachmentComponent,
		ProfileComponent,
		ImageAttachmentComponent,
	],
	imports: [
		BrowserModule,
		AppRoutingModule,
		FormsModule,
		HttpClientModule,
		AutosizeModule
	],
	providers: [],
	bootstrap: [AppComponent]
})
export class AppModule {
}
