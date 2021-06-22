# Highly Dependable Location Tracker

## Server Endpoints

## `POST /getReport`

### Description:
- Returns the location report of a user at an epoch


### Authorization Scope:
- User

### E.g. Request:
```
POST /getReport
Accept: application/json
{
    'userId': 1,
    'epoch': 1,
    'x': 0, //Useless for this endpoint
    'y': 0  //Useless for this endpoint
    
}
```

### E.g. Response:
```
200 OK

{
    'proofs':[{'nonce':epoch,'userId':x'pedidoProof':{'x':1,'y':2,'epoch':1,'proverId':'1','digitalSignature':''},'digitalSignature':''}],
    'x':1,
    'y':2,
    'epoch':1,
    'userId':1
    'digitalSignature':''
}
```

## `POST /submitReport`

### Description:
- User submits a location report


### Authorization Scope:
- Regular User

### E.g. Request:
```
POST /submitReport
Content-type: application/json

{
    'proofs':[{'nonce':epoch,'userId':x'pedidoProof':{'x':1,'y':2,'epoch':1,'proverId':'y','digitalSignature':''},'digitalSignature':''}],
    'x':1,
    'y':2,
    'epoch':1,
    'userId':y
    'digitalSignature':''
}
```

### E.g. Response:
```
200 OK
```

## `GET /locations/management`

### Description:
- Special User gets all location report of an epoch at location (x,y)

### Parameters:
- pos -  string
- epoch -  integer

### Authorization Scope:
- Special User

### E.g. Request:
```
GET /locations/management/1/2/1
Accept: application/json

{
    'userId': 0,    // Useless for this case
    'epoch': 1,
    'x': 2, 
    'y': 1  
    
}

```

### E.g. Response:
```
200 OK

{
    'users':[1,2,3,4],
    'digitalSignature':''
}
```

## Client Endpoints

## `GET /locations/{epoch}`

### Description:
- Asks to obtain the location of itself at a certain epoch.

### Parameters:
- epoch -  int

### Authorization Scope:
- User

### E.g. Request:
```
GET /locations/1
Accept: application/json
```

### E.g. Response:
```
200 OK

{
    'proofs':[{'nonce':epoch,'userId':x'pedidoProof':{'x':1,'y':2,'epoch':1,'proverId':'1','digitalSignature':''},'digitalSignature':''}],
    'x':1,
    'y':2,
    'epoch':1,
    'userId':1
    'digitalSignature':''
}
```

## `POST /proof`

### Description:
- Regular User asks for a location proof (x,y) to neighbors

### Parameters:
- userId : string

### Authorization Scope:
- Regular User

### E.g. Request:
```
POST /proof
Content-type: application/json

{
    'x':12,
    'y':8,
    'epoch':1,
    'proverId':'y'
    'digitalSignature':''
}
```

### E.g. Response:
```
200 OK

{
    'nonce':epoch,
    'userId':x
    'pedidoProof':{'x':12,'y':8,'epoch':1,'proverId':'y','digitalSignature':''},
    'digitalSignature':''
}
```
Note: If it's not a valid proof it responds with a 404 code

