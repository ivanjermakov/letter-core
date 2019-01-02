import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {API_URL} from '../../../globals';
import {Message} from '../component/dto/Message';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(private http: HttpClient) {
  }

  get(token: string, conversationId: number, offset: number, amount: number): Observable<Array<Message>> {
    return this.http.get<Array<Message>>(API_URL + '/message/get', {
      headers: {token: token},
      params: {
        conversationId: conversationId.toString(),
        offset: offset.toString(),
        amount: amount.toString()
      }
    });
  }

  delete(token: string, deleteMessages: Array<Message>) {
    return this.http.post(API_URL + '/message/delete', deleteMessages, {
      headers: {'Auth-Token': token}
    });
  }

}
