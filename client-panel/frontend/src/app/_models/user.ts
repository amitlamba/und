export class User {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
}

export class EventUser {
  id: string;
  clientId: string; //client id , user is associated with, this can come from collection
  clientUserId: string;//this is id of the user client has provided
  socialId: SocialId;
  standardInfo: StandardInfo;
  additionalInfo: object;
  //FIXME creation date can't keep changing
  creationDate: number

}

export class SocialId {
  fbId: string;
  googleId: string;
  mobile: string;
  email: string;
}

export class StandardInfo {
  firstName: string;
  lastName: string;
  gender: string;
  dob: string;
  country: string;
  countryCode: string;
}

export class UserParams {
  public static params = [
    "{user.socialId.email}",
    "{user.socialId.mobile}",
    "{user.standardInfo.firstName}",
    "{user.standardInfo.lastName}",
    "{user.standardInfo.gender}",
    "{user.standardInfo.dob}",
    "{user.standardInfo.country}",
    "{user.standardInfo.countryCode}"
  ];
}
