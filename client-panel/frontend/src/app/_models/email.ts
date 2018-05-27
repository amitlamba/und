import {Campaign} from "./campaign";

export class Email {
  clientID: number;
  fromEmailAddress: string;
  toEmailAddresses: string[];
  ccEmailAddresses: string[];
  bccEmailAddresses: string[];
  replyToEmailAddresses: string[];
  emailSubject: string;
  emailBody: string;
  userID: string;
}

export class EmailTemplate {
  id: number;
  clientID: number;
  appuserID: number;
  name: string;
  emailTemplateBody: string;
  emailTemplateSubject: string;
  parentID: number;
  from: string;
  messageType: MessageType;
  tags: string;
}

export enum MessageType{
  TRANSACTIONAL,
  PROMOTIONAL
}

export class EmailCampaign {
  emailCampaignId: number;
  emailCampaignClientID: number;
  appuserID: number;
  campaign: Campaign;
  emailTemplateID: number;
}
