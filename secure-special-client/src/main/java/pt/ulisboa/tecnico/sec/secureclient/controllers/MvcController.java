package pt.ulisboa.tecnico.sec.secureclient.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.models.AppRequestsModel;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;

@Controller
public class MvcController {
	
	private static final String APP_MODEL = "appModel";
	
	@Autowired
	private ISpecialUserService userService;
	
	@RequestMapping(value = {"/", "/index", "/home"})
	public String home() {
		System.out.println("Going home...");
		return "index";
	}
	
	@GetMapping("/user_report_form")
	public String getReportOfUserForm(Model model) {
		System.out.println("Gonna request a user report...");
		
		AppRequestsModel appModel = new AppRequestsModel();
		model.addAttribute(APP_MODEL, appModel);
		
		return "user_report_form";
	}
	
	@PostMapping("/user_report_form")
	public String getReportOfUserSubmit(@ModelAttribute("appModel") AppRequestsModel appModel) {
		System.out.println("Submitted a request for user report...");
		
		try {
			String result = this.userService.obtainLocationReport(SpecialClientApplication.userId, appModel.getUserId(), appModel.getEpoch()).toString();
			appModel.setResult(result);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
		//appModel.setResult("another result done!");
		
		return "user_report_success";
	}
	
	@GetMapping("/users_at_location_form")
	public String getUsersAtLocation(Model model) {
		System.out.println("Gonna request the users at a location...");
		
		AppRequestsModel report = new AppRequestsModel();
		model.addAttribute(APP_MODEL, report);
		
		return "users_at_location_form";
	}
	
	@PostMapping("/users_at_location_form")
	public String getUsersAtLocation(@ModelAttribute("appModel") AppRequestsModel appModel) {
		System.out.println("Gonna request the users at a location...");
		
		try {
			String result = this.userService.obtainUsersAtLocation(appModel.getUserId(), appModel.getX(), appModel.getY(), appModel.getEpoch()).toString();
			appModel.setResult(result);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
		//appModel.setResult("another result done!");
		
		return "users_at_location_success";
	}
	
	@GetMapping("/get_proofs_form")
	public String getProofs(Model model) {
		System.out.println("Gonna request the proofs of user...");
		
		AppRequestsModel appModel = new AppRequestsModel();
		model.addAttribute(APP_MODEL, appModel);
		
		return "get_proofs_form";
	}
	
	@PostMapping("/get_proofs_form")
	public String getProofs(@ModelAttribute("appModel") AppRequestsModel appModel) {
		System.out.println("Gonna request the proofs of user...");
		
		try {
			String result = this.userService.requestMyProofs(SpecialClientApplication.userId, appModel.getUserId(), appModel.getEpochsAsList()).toString();
			appModel.setResult(result);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
		//appModel.setResult("another result done!");
		
		return "get_proofs_success";
	}

}
