1. Run confluence with 

```bash
docker run --rm -p 8090:8090 -p 8091:8091 atlassian/confluence-server
```

2. Setup trial

3. Setup admin:admin user

4. Create `SAMPLE` namespace

5. Create `SN` namespace

6. Enable remote API

>  Choose the cog icon , then choose General Configuration under Confluence Administration.
>  Click Further Configuration in the left-hand panel.
>  Click Edit.
>  Click the check box next to Remote API (XML-RPC & SOAP).
>  Click Save.
